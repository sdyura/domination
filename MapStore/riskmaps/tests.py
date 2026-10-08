from django.test import TestCase
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
