import os
import shutil
from django.test import TestCase
from django.conf import settings
from django.db import connection
from django.contrib.auth.models import User
from riskmaps.models import GameMap


class MapListTest(TestCase):

    def setUp(self):
        self.author = User.objects.create(username='author')

    def add_map(self, file_name, visible=True):
        return GameMap.objects.create(
            name=file_name,
            description='a map',
            author=self.author,
            mapFile='2012-01-01-00-00-00/' + file_name,
            visible=visible)

    def get_map_urls(self, response):
        import re
        return re.findall(r'mapUrl="([^"]*)"', response.content)

    def test_search_by_mapfile(self):
        self.add_map('risk.map')
        self.add_map('Baldur\'s Gate.map')
        self.add_map('earth, sun, moon.map')
        self.add_map('Brazil_more(original)_bonuses.map')
        self.add_map('Gilbert@War.map')
        self.add_map('not requested.map')
        self.add_map('hidden.map', visible=False)

        response = self.client.post('/maps?format=xml&version=1.3.6beta1', {'mapfile': [
            'risk.map',
            'Baldur\'s Gate.map',
            'earth, sun, moon.map',
            'Brazil_more(original)_bonuses.map',
            'Gilbert@War.map',
            'hidden.map',
            'missing.map',
        ]})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(sorted(self.get_map_urls(response)), sorted([
            '/storage/2012-01-01-00-00-00/risk.map',
            '/storage/2012-01-01-00-00-00/Baldur&#39;s Gate.map',
            '/storage/2012-01-01-00-00-00/earth, sun, moon.map',
            '/storage/2012-01-01-00-00-00/Brazil_more(original)_bonuses.map',
            '/storage/2012-01-01-00-00-00/Gilbert@War.map',
        ]))

    def test_search_by_mapfile_does_not_treat_names_as_patterns(self):
        self.add_map('riskXmap.map')
        self.add_map('a_b.map')
        self.add_map('a%b.map')

        response = self.client.post('/maps?format=xml', {'mapfile': ['risk.map', 'a%b.map']})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(self.get_map_urls(response), ['/storage/2012-01-01-00-00-00/a%b.map'])

    def test_search_by_many_mapfiles(self):
        # the game client sends every map it has installed to check for updates,
        # this can be well over 1000 map files in a single request
        for i in range(0, 1500, 100):
            self.add_map('map %d.map' % i)

        map_files = ['map %d.map' % i for i in range(1500)]

        response = self.client.post('/maps?format=xml&version=1.3.6beta1', {'mapfile': map_files})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(self.get_map_urls(response)), 15)

    def count_queries(self, url):
        # connection.queries is only recorded when DEBUG is on, and the test runner turns it off
        old_debug = settings.DEBUG
        settings.DEBUG = True
        connection.queries = []
        try:
            response = self.client.get(url)
            self.assertEqual(response.status_code, 200)
            return len(connection.queries)
        finally:
            settings.DEBUG = old_debug

    def add_maps_with_new_authors(self, count):
        for i in range(count):
            self.author = User.objects.create(username='author %d' % User.objects.count())
            self.add_map('map %d.map' % GameMap.objects.count())

    def assert_query_count_does_not_grow(self, url):
        self.add_maps_with_new_authors(5)
        queries = self.count_queries(url)
        self.add_maps_with_new_authors(10)
        self.assertEqual(self.count_queries(url), queries)

    def test_list_xml_query_count_does_not_grow_with_maps(self):
        self.assert_query_count_does_not_grow('/?format=xml&sort=TOP_NEW')

    def test_list_html_query_count_does_not_grow_with_maps(self):
        self.assert_query_count_does_not_grow('/?sort=TOP_NEW')

    def test_list_xml_numbers(self):
        self.author = User.objects.create(username='someone', first_name='Some', last_name='One')
        game_map = self.add_map('risk.map')
        GameMap.objects.filter(id=game_map.id).update(numberOfDownloads=1234567, version=12)

        response = self.client.get('/?format=xml')

        self.assertEqual(response.status_code, 200)
        self.assertTrue('<Integer value="1"/>' in response.content)
        self.assertTrue('id="%d"' % game_map.id in response.content)
        self.assertTrue('authorId="%d"' % self.author.id in response.content)
        self.assertTrue('authorName="Some One"' in response.content)
        self.assertTrue('numberOfDownloads="1234567"' in response.content)
        self.assertTrue('version="12"' in response.content)

    def test_list_is_streamed_one_map_at_a_time(self):
        self.add_map('old.map')
        GameMap.objects.filter(name='old.map').update(dateAdded='2011-01-01 00:00:00')
        self.add_map('new.map')

        for url in ['/?format=xml&sort=TOP_NEW', '/?sort=TOP_NEW']:
            response = self.client.get(url)

            self.assertEqual(response.status_code, 200)
            # page before the list, one piece per map, page after the list
            self.assertEqual(len(list(response)), 4)

        response = self.client.get('/?format=xml&sort=TOP_NEW')
        self.assertTrue('<Integer value="2"/>' in response.content)
        self.assertEqual(self.get_map_urls(response), [
            '/storage/2012-01-01-00-00-00/new.map',
            '/storage/2012-01-01-00-00-00/old.map',
        ])
        self.assertTrue(response.content.endswith('</Task>\n'))

    def test_list_html_with_no_maps(self):
        response = self.client.get('/?sort=TOP_NEW')

        self.assertEqual(response.status_code, 200)
        self.assertTrue('No maps are available.' in response.content)


class MapListXmlTest(TestCase):
    """
    The game client parses this xml, so check the whole response against a saved copy
    """
    image_dir = 'test-map-list'

    def setUp(self):
        self.image_path = os.path.join(settings.MEDIA_ROOT, self.image_dir)
        os.makedirs(self.image_path)
        from PIL import Image
        Image.new('RGB', (677, 425), (10, 120, 200)).save(os.path.join(self.image_path, 'solar_pic.png'))

    def tearDown(self):
        shutil.rmtree(self.image_path)

    def add_map(self, **kwargs):
        date_added = kwargs.pop('dateAdded')
        game_map = GameMap.objects.create(**kwargs)
        GameMap.objects.filter(id=game_map.id).update(dateAdded=date_added)

    def test_list_xml(self):
        author = User.objects.create(id=7, username='someone', first_name='Some', last_name='One')
        no_name_author = User.objects.create(id=8, username='noname')
        self.add_map(id=101, name="Solar's \"Map\" & <more>", description='line one\nline "two" & <three>',
                     author=author, version=3, numberOfDownloads=1234567, visible=True,
                     mapFile='2011-11-13-12-07-50/solar.map', imageFile=self.image_dir + '/solar_pic.png',
                     dateAdded='2011-11-13 12:07:50')
        self.add_map(id=102, name='No Image', description='', author=no_name_author, visible=True,
                     mapFile='2012-01-01-00-00-00/noimage.map', dateAdded='2012-01-01 00:00:00')
        self.add_map(id=103, name='Hidden', description='not published', author=author, visible=False,
                     mapFile='2013-01-01-00-00-00/hidden.map', dateAdded='2013-01-01 00:00:00')

        response = self.client.get('/maps?format=xml&sort=TOP_NEW')

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response['Content-Type'], 'application/xml')
        expected_path = os.path.join(os.path.dirname(__file__), 'test_data', 'map_list.xml')
        if os.environ.get('WRITE_EXPECTED'):
            open(expected_path, 'w').write(response.content)
        self.assertEqual(response.content, open(expected_path).read())
