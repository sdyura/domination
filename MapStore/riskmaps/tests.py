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


class MapImageTest(TestCase):

    def setUp(self):
        import os, StringIO
        from PIL import Image
        from django.core.files.base import ContentFile
        from django.core.files.storage import default_storage
        self.dir = 'test-images-%d' % os.getpid()
        image = StringIO.StringIO()
        Image.new('RGB', (677, 425), (10, 120, 200)).save(image, 'JPEG')
        self.image_name = default_storage.save(self.dir + '/risk.jpg', ContentFile(image.getvalue()))
        self.image_path = default_storage.path(self.image_name)
        GameMap.objects.create(name='risk', description='a map', author=User.objects.create(username='author'),
            mapFile='2012-01-01-00-00-00/risk.map', imageFile=self.image_name, visible=True)

    def tearDown(self):
        import shutil
        shutil.rmtree(settings.MEDIA_ROOT + '/' + self.dir)

    def get_preview_url(self):
        import re
        response = self.client.get('/maps?format=xml&sort=TOP_NEW')
        self.assertEqual(response.status_code, 200)
        return re.findall(r'previewUrl="([^"]*)"', response.content)[0]

    def thumbnail_path(self, preview_url):
        self.assertTrue(preview_url.startswith(settings.MEDIA_URL + self.dir + '/'), preview_url)
        return settings.MEDIA_ROOT + '/' + preview_url[len(settings.MEDIA_URL):]

    def test_thumbnail_generated_when_missing(self):
        import os
        thumbnail = self.thumbnail_path(self.get_preview_url())
        self.assertTrue(os.path.exists(thumbnail))

        os.remove(thumbnail)
        self.assertEqual(self.thumbnail_path(self.get_preview_url()), thumbnail)
        self.assertTrue(os.path.exists(thumbnail))

    def test_existing_thumbnail_used_without_checking_source(self):
        import os, time
        thumbnail = self.thumbnail_path(self.get_preview_url())
        old_time = int(time.time()) - 1000
        os.utime(thumbnail, (old_time, old_time))

        # a source newer than its thumbnail would make easy-thumbnails regenerate it, but uploaded images never change
        stats = []
        real_stat = os.stat
        def counting_stat(path):
            stats.append(path)
            return real_stat(path)
        os.stat = counting_stat
        try:
            self.assertEqual(self.thumbnail_path(self.get_preview_url()), thumbnail)
        finally:
            os.stat = real_stat

        self.assertEqual(int(os.path.getmtime(thumbnail)), old_time)
        self.assertEqual(stats.count(self.image_path), 0)
        self.assertEqual(stats.count(thumbnail), 1)

    def get_size(self):
        import re
        response = self.client.get('/maps?format=xml&sort=TOP_NEW')
        self.assertEqual(response.status_code, 200)
        return re.findall(r'mapWidth="([^"]*)"\s+mapHeight="([^"]*)"', response.content)[0]

    def test_image_size_read_and_stored_when_missing(self):
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (None, None))
        self.assertEqual(self.get_size(), ('677', '425'))
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (677, 425))

    def test_stored_image_size_used_without_reading_image(self):
        import __builtin__
        self.get_preview_url()  # generate the thumbnail, which needs to read the image
        GameMap.objects.update(mapWidth=1000, mapHeight=500)

        opened = []
        real_open = __builtin__.open
        def counting_open(path, *args, **kwargs):
            opened.append(path)
            return real_open(path, *args, **kwargs)
        __builtin__.open = counting_open
        try:
            self.assertEqual(self.get_size(), ('1000', '500'))
        finally:
            __builtin__.open = real_open
        self.assertEqual(opened.count(self.image_path), 0)

        # clearing the stored size makes it read from the image again
        GameMap.objects.update(mapWidth=None, mapHeight=None)
        self.assertEqual(self.get_size(), ('677', '425'))
        self.assertEqual(GameMap.objects.values_list('mapWidth', 'mapHeight')[0], (677, 425))


class AddMissingColumnsTest(TestCase):

    def test_missing_columns_added_on_connect(self):
        import os, tempfile
        from django.core.management.color import no_style
        from django.db.backends.signals import connection_created
        from django.db.backends.sqlite3.base import DatabaseWrapper
        from riskmaps.models import add_missing_columns

        handle, path = tempfile.mkstemp(suffix='.db')
        os.close(handle)
        settings_dict = dict(connection.settings_dict, NAME=path)
        try:
            # a database made before mapWidth and mapHeight were added
            old_database = DatabaseWrapper(settings_dict)
            create_table = connection.creation.sql_create_model(GameMap, no_style(), set())[0][0]
            create_table = '\n'.join(line for line in create_table.split('\n') if '"mapWidth"' not in line and '"mapHeight"' not in line)
            cursor = old_database.cursor()
            cursor.execute(create_table)
            self.assertFalse('mapWidth' in self.columns(old_database))
            old_database.close()

            connection_created.connect(add_missing_columns)
            new_connection = DatabaseWrapper(settings_dict)
            new_connection.cursor()
            columns = self.columns(new_connection)
            self.assertTrue('mapWidth' in columns)
            self.assertTrue('mapHeight' in columns)
            new_connection.close()
        finally:
            os.remove(path)

    def columns(self, database):
        return [column[0] for column in database.introspection.get_table_description(database.cursor(), GameMap._meta.db_table)]
