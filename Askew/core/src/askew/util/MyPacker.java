package askew.util;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import java.util.Scanner;

public class MyPacker {
    public static void main (String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        System.out.println("inputDir");
        String inputDir = "texture/animated";
        System.out.println("outputDir");
        String outputDir = "texture/packed";
        System.out.println("packFileName");
        String packFileName = "packed";
        TexturePacker.process(inputDir, outputDir, packFileName);
    }
}