//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.tools {
import flash.display.BitmapData;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.utils.ByteArray;

import mx.graphics.codec.PNGEncoder;

public function savePng(fileName:String, bitmapData:BitmapData):BitmapData {
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
