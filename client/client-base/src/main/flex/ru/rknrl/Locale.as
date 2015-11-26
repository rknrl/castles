//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl {
import flash.utils.Dictionary;

public class Locale {
    /**
     * @param data  string in TSV format
     *              key1<tab>value1
     *              key2<tab>value2
     *              ...
     */
    public function Locale(data:String) {
        const lines:Array = data.split("\n");
        for each(var line:String in lines) {
            const split:Array = line.split("\t");
            if (split.length != 2) throw new Error("incorrect dictionary line " + line);
            dictionary[split[0]] = unescape(split[1]);
        }
    }

    private const dictionary:Dictionary = new Dictionary();

    protected final function translate(key:String, ...args):String {
        var s:String = dictionary[key];
        if (s == null) throw new Error("dictionary hasn't key " + key);
        for (var i:int = 0; i < args.length; i++) {
            s = s.replace("$" + (i + 1), args[i])
        }
        return s;
    }

    private static function unescape(s:String):String {
        return s.replace(/\\n/g, "\n");
    }
}
}
