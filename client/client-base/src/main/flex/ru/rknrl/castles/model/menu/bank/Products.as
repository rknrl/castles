//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.bank {
import protos.Product;
import protos.ProductId;

public class Products {
    private var products:Vector.<Product>;

    private var _product:Product;

    public function get product():Product {
        return _product;
    }

    public function Products(products:Vector.<Product>) {
        this.products = products;
        _product = getProductById(ProductId.STARS.id);
    }

    private function getProductById(id:int):Product {
        for each(var product:Product in products) {
            if (product.id == id) return product;
        }
        throw new Error("can't find product " + id);
    }
}
}
