package askew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;
import lombok.Setter;

import javax.xml.soap.Text;
import java.util.HashMap;
import java.util.Map;

/**
 * In addition to being the actual asset manager, this holds references to certain textures which require
 * processing on the openGL thread.
 */
public class MantisAssetManager extends AssetManager {

    public static final String WALL_TEXTURE = "texture/wall/wall.png";
    public static final String THORN_TEXTURE = "texture/thorn/thorns.png";
    private boolean loaded;
    private boolean preloaded;

    @Getter @Setter
    private String[] texturePaths;

    @Getter
    private Map<String,TextureRegion> processedTextureMap;

    @Getter
    private TextureAtlas textureAtlas;

    public MantisAssetManager() {
        super();
        processedTextureMap = new HashMap<>();
        textureAtlas = new TextureAtlas();
    }

    public void preloadProcess() {
        if (!preloaded) {
            load(WALL_TEXTURE, Texture.class);
            load(THORN_TEXTURE, Texture.class);
        }

        preloaded = true;
    }

    public void loadProcess() {
        if (!loaded) {
            createTexture(WALL_TEXTURE,true);
            createTexture(THORN_TEXTURE,true);
            textureAtlas = new TextureAtlas(Gdx.files.internal("texture/packed/packed.atlas"));
        }
        loaded = true;
    }

    /**
     * (originally from WorldController)
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param file		The texture (region) file
     * @param repeat	Whether the texture should be repeated
     *
     * @return a newly loaded texture region for the given file.
     */
    protected void createTexture(String file, boolean repeat) {
//        if (isLoaded(file)) {
            TextureRegion region = new TextureRegion(get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            processedTextureMap.put(file,region);
//        }
//        System.err.println("ERROR: Nonloaded: " + file);
    }
}
