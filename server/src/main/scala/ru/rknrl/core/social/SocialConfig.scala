//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.social

class SocialConfig(val appId: String,
                   val appSecret: String,
                   val productsInfo: List[ProductInfo])

class SocialConfigs(val vk: Option[SocialConfig],
                    val ok: Option[SocialConfig],
                    val mm: Option[SocialConfig],
                    val fb: Option[SocialConfig])