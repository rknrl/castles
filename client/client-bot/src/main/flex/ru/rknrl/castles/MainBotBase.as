//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.events.UncaughtErrorEvent;
import flash.system.Security;
import flash.text.TextField;
import flash.utils.ByteArray;
import flash.utils.setInterval;
import flash.utils.setTimeout;

import ru.rknrl.Log;
import ru.rknrl.asocial.ISocial;
import ru.rknrl.asocial.userInfo.Sex;
import ru.rknrl.asocial.userInfo.UserInfo;
import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.view.game.area.buildings.BuildingView;
import ru.rknrl.castles.view.game.ui.magicItems.GameMagicItem;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.menu.bank.Button;
import ru.rknrl.castles.view.menu.factory.CanvasFactory;
import ru.rknrl.castles.view.menu.main.SlotView;
import ru.rknrl.castles.view.menu.main.popups.BuildItem;
import ru.rknrl.castles.view.menu.main.popups.icons.UpgradeIcon;
import ru.rknrl.castles.view.menu.navigate.points.NavigationPoint;
import ru.rknrl.castles.view.menu.shop.ShopMagicItem;
import ru.rknrl.castles.view.menu.skills.FlaskView;
import ru.rknrl.dto.AccountId;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.PlatformType;
import ru.rknrl.dto.PlayerId;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SlotId;

public class MainBotBase extends Sprite {
    [Embed(source="/locale - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private var host:String;
    private var gamePort:int;
    private var policyPort:int;
    private var httpPort:int;
    private var accountId:AccountId;
    private var authenticationSecret:AuthenticationSecretDTO;
    private var social:ISocial;

    private var myUserInfo:UserInfo;
    private var main:Main;

    public function MainBotBase(host:String, gamePort:int, policyPort:int, httpPort:int, accountId:AccountId, authenticationSecret:AuthenticationSecretDTO, social:ISocial) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.httpPort = httpPort;
        this.accountId = accountId;
        this.authenticationSecret = authenticationSecret;
        this.social = social;
        Log.url = "http://" + host + ":" + httpPort + "/bug";
        Log.info("bugsLogUrl=" + Log.url);

        loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, onUncaughtError);

        Security.allowDomain("*");

        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);


        stage.addEventListener(Event.RESIZE, onResize);

        social.api.me(onGetMyUserInfo);
    }

    private function onGetMyUserInfo(userInfo:UserInfo):void {
        if (userInfo) {
            myUserInfo = userInfo;
            Log.info("myUserInfo: " + myUserInfo)
        } else {
            myUserInfo = new UserInfo({}, accountId.id, null, null, Sex.UNDEFINED);
            Log.info("myUserInfo fail");
        }
        start();
    }

    private function start():void {
        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();
        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);

        addChild(main = new Main(host, gamePort, policyPort, accountId, authenticationSecret, DeviceType.PC, PlatformType.CANVAS, localesUrl, defaultLocale, social, layout, new CanvasFactory(), myUserInfo));

        setTimeout(startBot, 3000)
    }

    private function onResize(event:Event):void {
        Log.info("resize " + stage.stageWidth + "x" + stage.stageHeight);
        if (main) main.layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);
    }

    private function get contentsScaleFactor():Number {
        return stage.hasOwnProperty("contentsScaleFactor") ? stage["contentsScaleFactor"] : 1;
    }

    private var hasError:Boolean;

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        if (!hasError) {
            hasError = true;

            const error:Error = event.error as Error;
            const stackTrace:String = error ? error.getStackTrace() : "";
            Log.error(event.error, stackTrace);
            if (main) main.addErrorScreen();
        }
    }

    private function startBot():void {
        /*
         clickSlot(SlotId.SLOT_3);
         setTimeout(function ():void {
         clickUpgradeButton();
         }, 2000);
         */

        /*
         clickSlot(SlotId.SLOT_1);
         setTimeout(function ():void {
         clickBuildButton();
         }, 2000);
         */

        /*
         setTimeout(function ():void {
         clickTextField("Играть");
         }, 2000);
         */

        /*
         setTimeout(function ():void {
         clickGameItem(ItemType.FIREBALL);
         }, 2000);

         setTimeout(function ():void {
         click(stage, main.layout.screenCenterX, main.layout.contentCenterY);
         }, 3000);
         */

        setInterval(function ():void {
            const fromBuilding:DisplayObject = findBuilding(DtoMock.playerId(0));
            const toBuilding:DisplayObject = findBuilding(DtoMock.playerId(1));
            const fromX:Number = fromBuilding.x + main.layout.gameAreaPos(15, 15).x;
            const fromY:Number = fromBuilding.y + main.layout.gameAreaPos(15, 15).y;
            const toX:Number = toBuilding.x + main.layout.gameAreaPos(15, 15).x;
            const toY:Number = toBuilding.y + main.layout.gameAreaPos(15, 15).y;

            mouseDown(stage, fromX, fromY);
            mouseMove(stage, toX, toY);
            mouseUp(stage, toX, toY);
        }, 2000);

        /*
         swipeScreen(ShopScreen);
         setTimeout(function ():void {
         buyItem(ItemType.FIREBALL);
         }, 2000);
         */
        /*
         swipeScreen(SkillsScreen);
         setTimeout(function ():void {
         buySkill(SkillType.DEFENCE);
         }, 2000);
         */
        /*
         swipeScreen(BankScreen);
         setTimeout(function ():void {
         buyStars();
         }, 2000);
         */
    }

    public function findBuilding(playerId:PlayerId):DisplayObject {
        return find(stage, function (a:DisplayObject):Boolean {
            const b:BuildingView = a as BuildingView;
            return b && (b.owner.equalsId(playerId));
        });
    }


    public function swipeScreen(screenClass:Class):void {
        click(find(stage, function (a:DisplayObject):Boolean {
            const point:NavigationPoint = a as NavigationPoint;
            return point && (point.screen is screenClass);
        }));
    }

    public function clickSlot(slotId:SlotId):void {
        return click(find(stage, function (a:DisplayObject):Boolean {
            const slot:SlotView = a as SlotView;
            return slot && (slot.id == slotId);
        }));
    }

    public function clickTextField(text:String):void {
        return click(find(stage, function (a:DisplayObject):Boolean {
            const textField:TextField = a as TextField;
            return textField && textField.text == text;
        }));
    }

    public function clickUpgradeButton():void {
        return click(find(stage, function (a:DisplayObject):Boolean {
            return a as UpgradeIcon;
        }));
    }

    public function clickBuildButton():void {
        return click(find(stage, function (a:DisplayObject):Boolean {
            return a as BuildItem;
        }));
    }

    public function buyItem(itemType:ItemType):void {
        click(find(stage, function (a:DisplayObject):Boolean {
            const item:ShopMagicItem = a as ShopMagicItem;
            return item && (item.itemType == itemType);
        }));
    }

    public function clickGameItem(itemType:ItemType):void {
        click(find(stage, function (a:DisplayObject):Boolean {
            const item:GameMagicItem = a as GameMagicItem;
            return item && (item.itemType == itemType);
        }));
    }

    public function buySkill(skillType:SkillType):void {
        click(find(stage, function (a:DisplayObject):Boolean {
            const flask:FlaskView = a as FlaskView;
            return flask && (flask.skillType == skillType);
        }));
    }

    public function buyStars():void {
        click(find(stage, function (a:DisplayObject):Boolean {
            return a as Button;
        }));
    }

    public static function mouseDown(displayObject:DisplayObject, x:Number = 1, y:Number = 1):void {
        displayObject.dispatchEvent(new MouseEvent(
                MouseEvent.MOUSE_DOWN, true, false,
                x, y
        ));
    }

    public static function mouseUp(displayObject:DisplayObject, x:Number = 1, y:Number = 1):void {
        displayObject.dispatchEvent(new MouseEvent(
                MouseEvent.MOUSE_UP, true, false,
                x, y
        ));
    }

    public static function mouseMove(displayObject:DisplayObject, x:Number = 1, y:Number = 1):void {
        displayObject.dispatchEvent(new MouseEvent(
                MouseEvent.MOUSE_MOVE, true, false,
                x, y
        ));
    }

    public static function click(displayObject:DisplayObject, x:Number = 1, y:Number = 1):void {
        mouseDown(displayObject, x, y);
        mouseUp(displayObject, x, y);
        displayObject.dispatchEvent(new MouseEvent(
                MouseEvent.CLICK, true, false,
                x, y
        ));
    }

    private static function find(container:DisplayObjectContainer, filter:Function):DisplayObject {
        for (var i:int = 0; i < container.numChildren; i++) {
            const child:DisplayObject = container.getChildAt(i);
            if (filter(child)) {
                return child;
            } else {
                const childContainer:DisplayObjectContainer = child as DisplayObjectContainer;
                if (childContainer) {
                    const result:DisplayObject = find(childContainer, filter);
                    if (result) return result;
                }
            }
        }
        return null;
    }
}
}
