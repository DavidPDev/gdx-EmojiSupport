# gdx-EmojiSupport
LibGDX Emoji support (glyph's injection "hack")

# Usage:
1) Just drop the EmojiSupport.java into your project
2) Copy the emoji atlas files into your assets folder
3) Add code support
```java
...
 EmojiSupport emojiSupport;   // global instance
 ...
 // Create instance, load emjis and add to existing font(s)
 emojiSupport= new EmojiSupport();
 emojiSupport.Load(Gdx.files.internal("data/emojis25.atlas"));
 emojiSupport.AddEmojisToFont(skin.getFont("default-font"));
 ...
 // Filter/translate emojis before display
 Label label1 = new Label(emojiSupport.FilterEmojis(str), skin);
```

# Description of the problem:
Current limitation: The Glyph class uses a char as index so should need a lot of work
to change all the logic (ex: line-breaks) to use multi-chars (unicode emojis).
So the solution is to "inject" new Gliphs (from another Texture page) to the
current BitmapFont using a special char index ( > 0xB000).
When a string includes an Emoji Code, for example: 😎 (\ud83d\ude0e) [two chars]
is replaced by a char 0xB001 [one char] that points to its emoji Glyph texture region.

# Benefits:
 - Only one emoji.png (Atlas) file needed (can be added to all fonts in the App)
 - Don't messes the GlyphsRun, BitmapFonts, BitmapFontCache, etc core Gdx classes
 - You can add only your "preferred" emojis (reduce space)
 - You can use emojis at greater or lower resolutions (will scale to the font size)
 - No cpu-cost in runtime (it's exactly the same as any other char)
 - Also supported in FreeType run-time generated fonts

# Drawbacks:
 - Always need to call to FilterEmojis to translate double-char codes (unicode) to 0xB000+i chars
   (This may not be a drawback as only in very few places you need to use texts with Emojis)
 - Text setColor() functions not supported (only in b&w emojis).
 
# To Do work:
 - Flags emojis and some other emojis are missing
 - Not supported skin-tone modifiers
 - Not tested in iOS (RoboVM)

# USEFUL LINKS:
 All Unicode chars : https://en.wikipedia.org/wiki/List_of_Unicode_characters
 
 Tables : https://www.utf8-chartable.de/unicode-utf8-table.pl
 
 Conversions : https://stackoverflow.com/questions/33320058/converting-unicode-symbols-to-their-code
 
 Java surrogate chars : https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html
 
 Special char FE0F : https://www.fileformat.info/info/unicode/char/fe0f/index.htm
 
 Emoji images source : https://github.com/stv0g/unicode-emoji
 
 Inspirated in : https://github.com/taluks/libgdx-emodji

