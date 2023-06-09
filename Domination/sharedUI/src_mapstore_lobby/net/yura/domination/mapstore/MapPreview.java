package net.yura.domination.mapstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.WeakHashMap;
import javax.microedition.lcdui.Image;
import net.yura.cache.Cache;
import net.yura.domination.engine.RiskUtil;
import net.yura.mobile.io.FileUtil;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.ImageUtil;

public class MapPreview {

    public static final String PREVIEW_FILE_PREFIX = "preview/";

    // this is a weak cache, it only keep a object if someone else holds it or a key
    // needs to be synchronizedMap or we get endless loop in WeakHashMap: http://www.adam-bien.com/roller/abien/entry/endless_loops_in_unsychronized_weakhashmap
    private static final java.util.Map mapCache = Collections.synchronizedMap(new WeakHashMap());
    
    private static Cache repo;

    static {
        try {
            repo = new Cache("net.yura.domination");
        }
        catch (Throwable ex) {
            System.err.println("[MapChooser] no cache: "+ex);
        }
    }

    /**
     * WARNING! this may be called from 2 threads at the same time
     * e.g. MapUpdateService.init and MapChooser.actionPerformed."local".run
     */
    public static Map createMap(String file) {

        WeakReference wr = (WeakReference)mapCache.get(file);
        if (wr!=null) {
            Map map = (Map)wr.get();
            if (map!=null) {
                return map;
            }
        }


        java.util.Map info = RiskUtil.loadInfo(file, false);

        Map map = new Map();
        map.setMapUrl( file );

        String name = (String)info.get("name");
        if (name==null) {
            if (file.toLowerCase().endsWith(".map")) {
                name = RiskUtil.getFileNameWithoutExtension(file);
            }
            else {
                name = file;
            }
        }
        map.setName(name);
        map.setDescription( (String)info.get("comment") );

        String prv = (String)info.get("prv");
        if (prv!=null) {
            prv = PREVIEW_FILE_PREFIX + prv;
            if (!fileExists(prv)) {
                prv=null;
            }
        }

        if (prv==null) {
            prv = (String)info.get("pic");
        }
        map.setPreviewUrl( prv );

        map.setVersion( (String)info.get("ver") );

        mapCache.put(file, new WeakReference(map));

        return map;
    }

    public static boolean haveLocalMap(String mapUID) {
        if (mapCache.containsKey(mapUID)) {
            return true;
        }
        return fileExists(mapUID);
    }

    public static boolean fileExists(String fileUID) {

        InputStream file=null;
        try {
            file = RiskUtil.openMapStream(fileUID);
        }
        catch (Exception ex) { } // not found?
        finally{
            FileUtil.close(file);
        }

        return (file != null); // we already have this file
    }

    public static void clearFromCache(String mapUID) {
        mapCache.remove(mapUID);
    }

    public static void cache(String url, byte[] data) {
        if (repo!=null && !repo.containsKey( url ) ) {
            repo.put( url , data );
        }
    }

    /**
     * @return map data or null if not saved in cache
     */
    public static InputStream getRemoteMapPreview(String url) {
        return repo != null ? repo.get(url) : null;
    }

    /**
     * can return null if unable to load image
     * @return the bytes of the map preview, could be the preview file itself or a cached generated preview
     */
    public static InputStream getLocalMapPreview(String url) {
        InputStream in;

        if (url.startsWith(PREVIEW_FILE_PREFIX)) {
            in = null;
            try {
                in = RiskUtil.openMapStream(url);

                if (in == null) {
                    throw new IllegalStateException("local preview stream null " + url);
                }
            }
            catch (Exception ex) {
                Logger.warn("cant open " + url, ex);
            }
        }
        else {
            in = repo != null ? repo.get(url) : null;

            if (in == null) {
                try {
                    System.out.println("[MapChooser] ### Going to re-encode img: "+url);
                    InputStream min = RiskUtil.openMapStream(url);
                    Image img = MapChooser.createImage(min);
                    img = ImageUtil.scaleImage(img, 150, 94);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageUtil.saveImage(img, out);
                    img = null; // drop the small image as soon as we can
                    byte[] bytes = out.toByteArray();
                    out = null; // drop the OutputStream as soon as we can
                    if (bytes.length == 0) {
                        throw new IllegalStateException("img failed to save " + url);
                    }
                    cache(url,bytes);
                    // TODO we should only cache if we are sure it can be opened as a image
                    in = new ByteArrayInputStream(bytes);
                }
                catch (OutOfMemoryError err) { // what can we do?
                    Logger.info("cant resize " + url, err);
                }
                catch (Exception ex) {
                    Logger.warn("cant resize " + url, ex);
                }
            }
        }

        return in;
    }
}
