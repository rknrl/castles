//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.bank {
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.ProductId;

public class Products {
    private var products:Vector.<ProductDTO>;

    private var _product:ProductDTO;

    public function get product():ProductDTO {
        return _product;
    }

    public function Products(products:Vector.<ProductDTO>) {
        this.products = products;
        _product = getProductById(ProductId.STARS.id());
    }

    private function getProductById(id:int):ProductDTO {
        for each(var product:ProductDTO in products) {
            if (product.id == id) return product;
        }
        throw new Error("can't find product " + id);
    }
}
}
