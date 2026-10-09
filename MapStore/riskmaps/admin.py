from riskmaps.models import GameCategory, GameMap, DailyStats
from django.contrib import admin

def approve(modeladmin, request, queryset):
    queryset.update(visible=True)

approve.short_description = "Approve selected maps"

class GameMapAdmin(admin.ModelAdmin):
    list_display = ['name','visible','dateAdded','numberOfDownloads']
    ordering = ['-dateAdded']
    actions = [approve]

    def save_model(self, request, obj, form, change):
        if 'imageFile' in form.changed_data:
            # the stored size is of the old image, it is read from the new one the next time the map is listed
            obj.mapWidth = obj.mapHeight = None
        obj.save()

class DailyStatsAdmin(admin.ModelAdmin):
    list_display = ['date', 'numberOfDownloads']
    ordering = ['-date']

admin.site.register(GameMap, GameMapAdmin)
admin.site.register(GameCategory)
admin.site.register(DailyStats, DailyStatsAdmin)
