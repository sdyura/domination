from django.db import models
from djangoratings.fields import RatingField
from django.contrib.auth.models import User
from datetime import datetime


def map_file_path(instance):
    if not instance.cuntDate:
        import datetime
        instance.cuntDate = datetime.datetime.now()

    return instance.cuntDate.strftime("%Y-%m-%d-%H-%M-%S")

def map_file_name(instance, filename):
    return map_file_path(instance) + "/" + filename

def preview_file_name(instance, filename):
    return map_file_path(instance) + "/preview/" + filename

class DailyStats(models.Model):
    date = models.DateField(blank=True, auto_now_add=True)
    numberOfDownloads = models.PositiveIntegerField(blank=True, default=0)

    class Meta:
        verbose_name = "Daily Stat"
        verbose_name_plural = "Daily Stats"

    @staticmethod
    def stat_for_today():
        stats = DailyStats.objects.filter(date=datetime.now())

        if len(stats) > 0:
            return stats[0]
        else:
            return DailyStats()

class GameCategory(models.Model):
    name = models.CharField(max_length=64,unique=True)
    icon = models.ImageField(upload_to='category-icons', blank=True)

    class Meta:
        verbose_name = "Category"
        verbose_name_plural = "Categories"

    def __unicode__(self):
        return self.name


class GameMap(models.Model):
    name = models.CharField(max_length=128)
    description = models.TextField()
    author = models.ForeignKey(User, blank=True)
    categories = models.ManyToManyField(GameCategory)

    version = models.PositiveIntegerField(blank=True, default=1)
    dateAdded = models.DateTimeField(blank=True, auto_now_add=True)

    cuntDate = None 

    # Popularity
    numberOfDownloads = models.PositiveIntegerField(blank=True, default=0)
    rating = RatingField(range=5, allow_anonymous=True, use_cookies=True)

    # Actual File Data
    outlinesFile = models.FileField(upload_to=map_file_name, blank=True)
    imageFile = models.ImageField(upload_to=map_file_name, blank=True)
    cardsFile = models.FileField(upload_to=map_file_name, blank=True)
    mapFile = models.FileField(upload_to=map_file_name, blank=True)
    prevFile = models.FileField(upload_to=preview_file_name, blank=True)

    #previewUrl = models.URLField()
    #mapUrl = models.URLField()
    #mapWidth = models.PositiveIntegerField()
    #mapHeight = models.PositiveIntegerField()

    visible = models.BooleanField(default=False)

    class Meta:
        verbose_name = "Map"
        verbose_name_plural = "Maps"

#    def __init__(self):
#        import datetime
#        self.cuntDate = datetime.datetime.now()

    def __unicode__(self):
        if self.author:
            return self.name + " (By " + self.author.username + ")"
        else:
            return self.name

    def save(self, *args, **kwargs):
        super(GameMap, self).save(*args, **kwargs)

