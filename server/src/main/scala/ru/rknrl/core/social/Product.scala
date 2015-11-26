//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.social

/**
 * Поля одинаковые для всех соц. сетей
 *
 * @param id    id товара в приложении
 *              должен быть больше 0
 *              Не string потому что в моем мире uint
 * @param title название товара
 *              В вконтакте не более 48 символов
 *              В моем мире не более 40 символов
 * @param description описание товара
 * @param photoUrl    URL изображения товара на сервере разработчика.
 *                    В вконтакте екомендуемый размер изображения – 75х75px.
 */
class Product(val id: Int,
              val title: String,
              val description: String,
              val photoUrl: String) {
  assert(id > 0)
  assert(title.size <= 40)
}

/**
 * Поля различные для каждой соц. сети
 *
 * @param id        id товара в приложении
 * @param count     кол-во единиц товара
 * @param price     стоимость в валюте соц. сети
 * @param currency  валюта соц. сети
 *
 * @example       100 алмазов за один голос: new ProductInfo(almazId, count=100, price=1, currency="голос")
 */
class ProductInfo(val id: Int,
                  val count: Int,
                  val price: Int,
                  val currency: String) {
  assert(price > 0)
}
