package ru.rknrl.castles.tools {
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.utils.ByteArray;

import mx.graphics.codec.PNGEncoder;

public function savePng(fileName:String, displayObject:DisplayObject, bg:BitmapData):BitmapData {
    const bitmapData:BitmapData = bg.clone();
    bitmapData.draw(displayObject);

    const byteArray:ByteArray = new PNGEncoder().encode(bitmapData);
    const file:File = new File(File.applicationDirectory.nativePath + "/" + fileName + ".png");
    trace(file.nativePath);
    const fileStream:FileStream = new FileStream();
    fileStream.open(file, FileMode.WRITE);
    fileStream.writeBytes(byteArray);
    fileStream.close();

    return bitmapData;
}
}
