package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.ArrayList;
import java.util.HashMap;

/* Description of the problem:
*  Current limitation: The Glyph class uses a char as index so should need a lot of work
*  to change all the logic (ex: line-breaks) to use multi-chars (unicode emojis).
*  So the solution is to "inject" new Gliphs (from another Texture page) to the
*  current BitmapFont using a special char index ( > 0xB000).
*  When a string includes an Emoji Code, for example: 😎 (\ud83d\ude0e) [two chars]
*  is replaced by a char 0xB001 [one char] that points to its emoji Glyph texture region.
* Benefits:
*  - Only one emoji.png (Atlas) file needed (can be added to all fonts in the App)
*  - Don't messes the GlyphsRun, BitmapFonts, BitmapFontCache, etc core Gdx classes
*  - You can add only your "preferred" emojis (reduce space)
*  - You can use emojis at greater or lower resolutions (will scale to the font size)
*  - No cpu-cost in runtime (it's exactly the same as any other char)
*  - Also supported in FreeType run-time generated fonts
* Drawbacks:
*  - Always need to call to FilterEmojis to translate double-char codes (unicode) to 0xB000+i chars
*    (This may not be a drawback as only in very few places you need to use texts with Emojis)
*  - Text setColor() functions not supported (only in b&w emojis).
* To Do work:
*  - Flags emojis and some other emojis are missing
*  - Not supported skin-tone modifiers
*  - Not tested in iOS (RoboVM)
*
* USEFUL LINKS:
*  All Unicode chars : https://en.wikipedia.org/wiki/List_of_Unicode_characters
*  Tables : https://www.utf8-chartable.de/unicode-utf8-table.pl
*  Conversions : https://stackoverflow.com/questions/33320058/converting-unicode-symbols-to-their-code
*  Java surrogate chars : https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html
*  Special char FE0F // https://www.fileformat.info/info/unicode/char/fe0f/index.htm
*  Emoji images source : https://github.com/stv0g/unicode-emoji
*  Inspirated in : https://github.com/taluks/libgdx-emodji
*
* Sample usage:
*  ...
*  EmojiSupport emojiSupport;   // global instance
*  ...
*  // Create instance, load emjis and add to existing font(s)
*  emojiSupport= new EmojiSupport();
*  emojiSupport.Load(Gdx.files.internal("data/emojis25.atlas"));
*  emojiSupport.AddEmojisToFont(skin.getFont("default-font"));
*  ...
*  // Filter/translate emojis before display
*  Label label1 = new Label(emojiSupport.FilterEmojis(str), skin);
*
* CHANGELOG
*  - v.1.2 - Minor change in Load(). ".texture" (direct access to property) -> ".getTexture()" (use of getter)
*  - v.1.1 - Support for multi pages emojis (bugfix)
*  - v.1.0 - Initial release
*/

public class EmojiSupport
{
    private final static String VERSION = "1.2";    // Current version of EmojiSupport helper

    public final static char START_CHAR = 0xB000;	// Starting replacement-chars (very rarely used range)

    public class EmojiRegionIndex {
        int index;                              // Index starting from START_CHAR
        int page;                               // Multiple pages of emojis
        TextureAtlas.AtlasRegion atlasRegion;   // Region data
        EmojiRegionIndex (int i, int p, TextureAtlas.AtlasRegion al) { index=i; page=p; atlasRegion=al; }
    }

    public TextureAtlas textureAtlas;                   // Emojis texture atlas
    public ArrayList<Texture> textures;                 // If multiple pages of emojis
    public HashMap<Integer, EmojiRegionIndex> regions;  // Maps unicode emoji -> injected index

    public void Load (FileHandle fileHandle) {
        // Default Linear filter (sometimes Nearest looks better with emojis)
        Load (fileHandle, Texture.TextureFilter.Linear);
    }
    public void Load (FileHandle fileHandle, Texture.TextureFilter textureFilter) {
        textureAtlas= new TextureAtlas(fileHandle);
        textures= new ArrayList<>();
        for (Texture t : textureAtlas.getTextures()) {
            t.setFilter(textureFilter, textureFilter);
            textures.add(t);
        }

        regions= new HashMap<Integer, EmojiRegionIndex>();
        Array<TextureAtlas.AtlasRegion> regs= textureAtlas.getRegions();
        for (int i=0; i<regs.size; i++) {
            try {
                int unicodeCode = Integer.parseInt(regs.get(i).name, 16);
                int page=0;
                if (textures.size()>1) {    // Multi pages emojis, we must find the page where it's located
                    for (int j=0; j<textures.size(); j++) {
                        if (textures.get(j).hashCode()==regs.get(i).getTexture().hashCode()) { page=j; break; }
                    }
                }
                regions.put(unicodeCode, new EmojiRegionIndex(i, page, regs.get(i)));
            }
            catch (Exception e) {
                // Maybe error in name (not hex integer)
                throw new GdxRuntimeException("Invalid emoji (Not valid Hex code): " + regs.get(i).name);
            }
        }
    }
    public void AddEmojisToFont (BitmapFont bitmapFont) {
        // 1- Add New TextureRegion
        Array<TextureRegion> regs= bitmapFont.getRegions();
        int pageLast= regs.size;
        for (Texture t: textures) regs.add (new TextureAtlas.AtlasRegion(t, 0,0, t.getWidth(), t.getHeight()));

        // 2- Add all emoji glyphs to font
        int size= (int)(bitmapFont.getData().lineHeight/bitmapFont.getData().scaleY);
        for (EmojiRegionIndex entry : regions.values()) {
            char ch= (char)(START_CHAR + entry.index);
            BitmapFont.Glyph glyph= bitmapFont.getData().getGlyph(ch);
            if (glyph==null) {	// Add Emoji as new Glyph (only if not exists in font)
                glyph= new BitmapFont.Glyph();
                glyph.id= ch;
                glyph.srcX = 0;
                glyph.srcY = 0;
                glyph.width = size;
                glyph.height = size;
                glyph.u = entry.atlasRegion.getU();
                glyph.v = entry.atlasRegion.getV2(); 	// Inverted y-axis (?)
                glyph.u2 = entry.atlasRegion.getU2();
                glyph.v2 = entry.atlasRegion.getV();
                glyph.xoffset = 0;
                glyph.yoffset = -size;
                glyph.xadvance = size;
                glyph.kerning = null;
                glyph.fixedWidth = true;
                glyph.page = pageLast + entry.page;
                bitmapFont.getData().setGlyph(ch,glyph);
            }
        }
    }
    public String FilterEmojis (String str)	{ // Translates str replacing emojis with its 0xB000+i index

        if (str==null || str.length()==0) return str;

        int length= str.length();
        int i=0;
        StringBuilder sb= new StringBuilder();
        while (i<length) {
            char ch= str.charAt(i);
            boolean isCharSurrogate=  (ch>='\uD800' && ch<='\uDFFF'); // Special 2-chars surrogates (uses two chars)
            boolean isCharVariations= (ch>='\uFE00' && ch<='\uFE0F'); // Special char for skin-variations (omit)
            int codePoint= str.codePointAt(i);
            EmojiRegionIndex eri= regions.get(codePoint);
            if (eri!=null) sb.append((char)(START_CHAR+eri.index));         // Add found emoji
            else if (!isCharSurrogate && !isCharVariations) sb.append(ch);  // Exclude special chars
            i += isCharSurrogate ? 2 : 1;                                   // Surrogate chars use 2 characters
        }
        return sb.toString();
    }
}
