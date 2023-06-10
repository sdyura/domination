package net.yura.domination.lobby.mini;

import java.util.List;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapPreview;
import net.yura.domination.mapstore.MapServerClient;
import net.yura.domination.mapstore.MapServerListener;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.logging.Logger;

/**
 * TODO
 * more then one instance of this is created on the desktop version of the game
 * this is not good as we have multiple MapServerClients potentially downloading the same files at the same time
 */
public abstract class MapPreviewClient implements MapServerListener {

    private MapServerClient mapServerClient;

    /**
     * This will ALWAYS return an icon, but the icon MAY not be able to draw yet.
     * will also trigger the {@link #publishMap(net.yura.domination.mapstore.Map) } callback,
     * if the icons is unable to draw, when it becomes able to draw the {@link #publishImg(java.lang.String) } will get called.
     */
    public Icon getIconForMap(String mapUID) {

        // there are 3 layers of WeakHashMap
        // for locale maps:
        //      Game -> MapUID {@link MiniLobbyRisk#mapping} (added here)
        //      MapUID -> Map {@link MapChooser#mapCache} (added in MapChooder.getLocalIconForMap -> MapChooser.getIconForMapOrCategory)
        //      Map -> LazyIcon {@link MapChooser#iconCache} (added in MapChooder.getLocalIconForMap -> MapChooser.getIconForMapOrCategory -> MapChooser.gotImg)
        // for remote maps
        //      Game -> MapUID {@link MiniLobbyRisk#mapping} (added here)
        //      MapUID -> LazyIcon  {@link MapChooser#iconCache} (MiniLobbyRisk.mapServerClient.gotResultMaps -> MapChooser.getRemoteImage -> MapChooser.gotImg)

        // if local map
        if (MapPreview.haveLocalMap(mapUID)) {
            Map map = MapPreview.createMap(mapUID);
            publishMap(map);
            return MapPreview.getLocalIconForMap(map);
        }

        if (mapServerClient == null) {
            mapServerClient = new MapServerClient(this);
            mapServerClient.start();
        }

        return MapPreview.getRemoteIconForMap(mapUID, mapServerClient);
    }

    public void shutdown() {
        if (mapServerClient != null) {
            mapServerClient.kill();
            mapServerClient = null;
        }
    }

    public abstract void publishMap(Map map);

    public abstract void publishImg(String mapUID);




    // implementation of MapServerListener

    public void gotResultCategories(String url, List categories) { }
    public void gotResultMaps(String url, List maps) {
        if (maps.size() != 1) {
            Logger.warn("wrong number of maps found on MapServer for " + url + " " + maps);
            return;
        }
        Map map = (Map) maps.get(0);

        publishMap(map);

        Object mapUIDkey = MapPreview.getFileUID(map.getMapUrl());
        boolean fromCache = MapPreview.getRemoteImage(mapUIDkey, MapPreview.getURL(url, map.getPreviewUrl()), mapServerClient);

        if (fromCache) {
            publishImg(mapUIDkey);
        }
    }
    public void onXMLError(String string) {
        Logger.info("ERROR " + string);
    }
    public void downloadFinished(String mapUID) { }
    public void onDownloadError(String string) { }
    
    public void publishImg(Object key) {
        publishImg((String)key);
    }
}
