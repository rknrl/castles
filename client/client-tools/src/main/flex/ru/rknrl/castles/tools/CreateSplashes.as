//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.tools {
import flash.display.BitmapData;
import flash.display.Sprite;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.loading.LoadingScreen;

public class CreateSplashes extends Sprite {
    private static const portrait:Vector.<Splash> = new <Splash>[
        new Splash("Default.png", 320, 480),
        new Splash("Default@2x.png", 640, 960),
        new Splash("Default-568h@2x.png", 640, 1136)
    ];

    private static const landscape:Vector.<Splash> = new <Splash>[
        new Splash("Default-Landscape.png", 1024, 768),
        new Splash("Default-Landscape@2x.png", 2048, 1536)
    ];

    public function CreateSplashes() {
        for each(var splash:Splash in portrait) {
            create(splash, new LayoutPortrait(splash.width, splash.height, 1));
        }
        for each(splash in landscape) {
            create(splash, new LayoutLandscape(splash.width, splash.height, 1));
        }
    }

    private function create(splash:Splash, layout:Layout):BitmapData {
        const screen:LoadingScreen = new LoadingScreen("", layout);
        const bitmapData:BitmapData = new BitmapData(splash.width, splash.height, false, 0xffffff);
        bitmapData.draw(screen, null, null, null, null, true);
        return bitmapData;
    }
}
}

class Splash {
    public var name:String;
    public var width:int;
    public var height:int;

    public function Splash(name:String, width:int, height:int) {
        this.name = name;
        this.width = width;
        this.height = height;
    }
}
