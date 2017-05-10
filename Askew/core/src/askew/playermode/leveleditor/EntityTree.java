package askew.playermode.leveleditor;

import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class EntityTree {

    private final ETNode root;
    ETNode current;
    boolean isBackground;

    public EntityTree(){
        root = new ETNode(null, "Entities", "texture/leveleditor/placeholder.png", false);

        ETNode backgrounds = new ETNode(root, "Background", "texture/background/fern.png", false);
        ETNode sloth = new ETNode(root, "SlothModel", "texture/sloth/frontflow.png", true);
        ETNode vine = new ETNode(root,"Vine", "texture/vine/vine.png", true);
        ETNode owlmodel = new ETNode(root,"OwlModel", "texture/leveleditor/placeholder.png", true);
        ETNode sticks = new ETNode(root,"Sticks", "texture/branch/branch.png", false);
        ETNode trunk = new ETNode(sticks,"Trunk", "texture/leveleditor/placeholder.png", true);
        ETNode polevault = new ETNode(sticks,"PoleVault", "texture/leveleditor/placeholder.png", true);
        ETNode stiffbranch = new ETNode(sticks,"StiffBranch", "texture/leveleditor/placeholder.png", true);

        ETNode walls = new ETNode(root,"Walls", "texture/leveleditor/placeholder.png", false);
        ETNode wallmodel = new ETNode(walls,"WallModel", "texture/leveleditor/placeholder.png", true);
        ETNode thorn = new ETNode(walls,"ThornModel", "texture/leveleditor/placeholder.png", true);
        ETNode enemies = new ETNode(root,"Enemies", "texture/leveleditor/placeholder.png", false);

        ETNode ghostmodel = new ETNode(enemies,"GhostModel", "texture/leveleditor/placeholder.png", true);

        ETNode eyes = new ETNode(root, "EyeEntity", "texture/eye/eyes.png", true);
//        root.add(backgrounds);
//        root.add(sloth);
//        root.add(vine);
//        root.add(owlmodel);
//        root.add(sticks);
//            sticks.add(trunk);
//            sticks.add(polevault);
//            sticks.add(stiffbranch);
//        root.add(walls);
//            walls.add(wallmodel);
//            walls.add(thorn);
//        root.add(enemies);
//            enemies.add(ghostmodel);

        current = root;
        isBackground = false;
    }

    public void setCurrent(ETNode e){
        current = e;
        if(current.name.equals("Background")){
            isBackground = true;
        }
    }

    public void upFolder(){
        if(current.name.equals("Background")){
            isBackground = false;
        }
        current = current.parent;
    }

    public void setTextures(MantisAssetManager manager){
        String[] allPaths = manager.getTexturePaths();
        //System.out.println("Background Entities:");
        for (String s : allPaths){
            if(s.contains("/background/")){
                String[] removebg = s.split("/background/");
                String[] parts = removebg[1].split("/");
                ETNode node = root.children.get(0);
                for(int i = 0; i < parts.length; i++){
                    String str = parts[i];
                    if(i== parts.length-1){
                        str = str.substring(0,str.indexOf("."));
                        ETNode bg = new ETNode(node, str, s, true);
                    }
                    else{
                        ETNode temp = node.find(parts[i]);
                        if(temp != null){
                            node = temp;
                        }
                        else{
                            node = new ETNode(node, str,"texture/leveleditor/folder.png" , false);
                        }
                    }
                }
            }
        }
        setTextures(manager,root);
        //System.out.println("Entity Tree Structure: ");
        //System.out.println(this);

    }

    private void setTextures(MantisAssetManager manager, ETNode node){
        node.texture = manager.get(node.texturePath);
        if(!node.isLeaf){
            for(ETNode e : node.children){
                setTextures(manager, e);
            }
        }
    }

    public String toString(){
        return root.toString();
    }

    public class ETNode {

        final String name;
        final String texturePath;//maybe string
        Texture texture = null;
        final ETNode parent;
        ArrayList<ETNode> children;
        final boolean isLeaf;

        public ETNode(ETNode parent, String name, String texturePath, boolean isLeaf){
            this.name = name;
            this.texturePath = texturePath;
            this.parent = parent;
            this.isLeaf = isLeaf;
            if(!isLeaf){
                children = new ArrayList<>();
            }
            if(parent != null)
                parent.add(this);
        }

        public void add(ETNode e){
            children.add(e);
        }

        public ETNode find( String s){
            for(ETNode child : children){
                if(child.name.equals(s)){
                    return child;
                }
            }
            return null;
        }

        public String toString(){
            return toString(this, "");
        }
        private String toString(ETNode node, String parent){
            String ans = parent + node.name + "\n";
            if(!node.isLeaf){
                for(ETNode e : node.children){
                    ans += toString(e, parent  + node.name + "/" );
                }
            }
            return ans;
        }
    }
}
