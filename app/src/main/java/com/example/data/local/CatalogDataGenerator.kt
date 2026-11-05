package com.example.data.local

import com.example.data.model.MarketPrice
import com.example.data.model.ShoppingItem

/**
 * Generates an extensive catalog of 1,000 realistic Algerian grocery, supermarket,
 * dairy, butcher, beverage, produce, bakery, and household products with real-world DZD pricing.
 */
object CatalogDataGenerator {

    private data class ProductTemplate(
        val baseEn: String,
        val baseAr: String,
        val category: String,
        val defaultPriceDzd: Double,
        val brands: List<Pair<String, String>>, // Pair(BrandEn, BrandAr)
        val units: List<String>
    )

    private val templates = listOf(
        // Dairy & Cheese (ألبان وأجبان)
        ProductTemplate(
            baseEn = "Fresh Milk",
            baseAr = "حليب طازج",
            category = "Dairy",
            defaultPriceDzd = 110.0,
            brands = listOf("Candia" to "كانديا", "Loya" to "لويا", "Soummam" to "صومام", "Trefle" to "تريفلي", "Hodna" to "حضنة", "Giprait" to "جيبريليت"),
            units = listOf("1L", "500ml", "بلاستيك 1 لتر", "كرتون 1L")
        ),
        ProductTemplate(
            baseEn = "Natural Yogurt",
            baseAr = "ياغورت طبيعي",
            category = "Dairy",
            defaultPriceDzd = 25.0,
            brands = listOf("Soummam" to "صومام", "Danone" to "دانون", "Hodna" to "حضنة", "Trefle" to "تريفلي", "Palmary" to "بالماري"),
            units = listOf("Pot 100g", "علبة 100غ", "Lot de 4", "Pack 8x100g")
        ),
        ProductTemplate(
            baseEn = "Fruit Yogurt",
            baseAr = "ياغورت بالفواكه",
            category = "Dairy",
            defaultPriceDzd = 35.0,
            brands = listOf("Soummam" to "صومام", "Danone" to "دانون", "Mamie Nova" to "مامي نوفا", "Hodna" to "حضنة"),
            units = listOf("Fraise 100g", "فراولة", "Pêche 100g", "خوخ", "Banane 100g", "موز")
        ),
        ProductTemplate(
            baseEn = "Portion Cheese",
            baseAr = "جبن مثلثات",
            category = "Dairy",
            defaultPriceDzd = 220.0,
            brands = listOf("Berbère" to "بربر", "La Vache Qui Rit" to "البقرة الضاحكة", "Président" to "بريزيدون", "Tartino" to "تارتينو", "Maison" to "ميزون"),
            units = listOf("8 Portions", "16 Portions", "24 Portions", "32 Portions")
        ),
        ProductTemplate(
            baseEn = "Grated Gruyere Cheese",
            baseAr = "جبن غرويير مبشور",
            category = "Dairy",
            defaultPriceDzd = 190.0,
            brands = listOf("Mondor" to "موندور", "Président" to "بريزيدون", "Berbère" to "بربر", "Fraicheur" to "فريشور"),
            units = listOf("100g", "150g", "200g", "500g")
        ),
        ProductTemplate(
            baseEn = "Cheddar & Edam Block",
            baseAr = "قالب جبن شيدر و إيدام",
            category = "Dairy",
            defaultPriceDzd = 450.0,
            brands = listOf("Mondor" to "موندور", "Berbère" to "بربر", "Tartino" to "تارتينو", "Palmary" to "بالماري"),
            units = listOf("250g", "350g", "500g", "1kg")
        ),
        ProductTemplate(
            baseEn = "Fresh Butter",
            baseAr = "زبدة طازجة ولذيذة",
            category = "Dairy",
            defaultPriceDzd = 280.0,
            brands = listOf("Président" to "بريزيدون", "Candia" to "كانديا", "Lactel" to "لاكتيل", "Hodna" to "حضنة"),
            units = listOf("200g", "250g", "500g")
        ),

        // Pantry & Cooking Staples (المواد الغذائية والتموين)
        ProductTemplate(
            baseEn = "Cooking Table Oil",
            baseAr = "زيت المائدة الصافي",
            category = "Pantry",
            defaultPriceDzd = 135.0,
            brands = listOf("Elio" to "إيليو", "Afia" to "عافية", "Cevital" to "سيفيتال", "Fleurial" to "فلوريال", "Safia" to "صافية"),
            units = listOf("1L", "2L", "5L", "قنينة 5 لتر")
        ),
        ProductTemplate(
            baseEn = "Refined White Sugar",
            baseAr = "سكر أبيض نقي ومكرر",
            category = "Pantry",
            defaultPriceDzd = 95.0,
            brands = listOf("Cevital" to "سيفيتال", "Ennour" to "النور", "La Gazelle" to "الغزالة"),
            units = listOf("1kg", "2kg", "5kg", "قطع كرتون 1kg")
        ),
        ProductTemplate(
            baseEn = "Semolina Wheat",
            baseAr = "سميد قمح ممتاز",
            category = "Pantry",
            defaultPriceDzd = 120.0,
            brands = listOf("Sim" to "سيم", "Mama" to "ماما", "Benamor" to "بن عمر", "Safina" to "سفينة"),
            units = listOf("Fin 1kg", "رقيق 1 كغ", "Moyen 1kg", "متوسط 1 كغ", "5kg", "10kg", "25kg كيس")
        ),
        ProductTemplate(
            baseEn = "Couscous Traditional",
            baseAr = "كسكس تقليدي جزائري",
            category = "Pantry",
            defaultPriceDzd = 150.0,
            brands = listOf("Sim" to "سيم", "Mama" to "ماما", "Benamor" to "بن عمر", "Safina" to "سفينة", "Beldi" to "بلدي"),
            units = listOf("Fin 1kg", "Moyen 1kg", "Gros 1kg", "قمح كامل 1kg")
        ),
        ProductTemplate(
            baseEn = "Traditional Pasta & Macaroni",
            baseAr = "معكرونة ومقرونة متنوعة",
            category = "Pantry",
            defaultPriceDzd = 85.0,
            brands = listOf("Mama" to "ماما", "Sim" to "سيم", "Benamor" to "بن عمر", "Panzani" to "بانزاني"),
            units = listOf("Coquillettes 500g", "Spaghetti 500g", "سباغيتي 500غ", "Penne 500g", "Torsades 500g", "شوربة فريك 500غ")
        ),
        ProductTemplate(
            baseEn = "Concentrated Tomato Paste",
            baseAr = "طماطم مصبرة مركزة",
            category = "Pantry",
            defaultPriceDzd = 140.0,
            brands = listOf("Cab" to "كاب", "Amor Benamor" to "عمر بن عمر", "Izmir" to "إزمير", "Jumairah" to "جميرة", "N’gaous" to "نقاوس"),
            units = listOf("400g", "800g", "علبة 800غ", "1kg", "4kg سطل")
        ),
        ProductTemplate(
            baseEn = "Roasted Ground Coffee",
            baseAr = "قهوة مطحونة ومحمصة",
            category = "Pantry",
            defaultPriceDzd = 320.0,
            brands = listOf("Famico" to "فاميكو", "Bonal" to "بونال", "Aroma" to "أروما", "Mellah" to "ملاح", "Nescafé" to "نسكافيه"),
            units = listOf("250g", "باكي 250غ", "500g", "حبوب 1kg")
        ),
        ProductTemplate(
            baseEn = "Green & Black Tea",
            baseAr = "شاي أخضر وأسود أصيل",
            category = "Pantry",
            defaultPriceDzd = 180.0,
            brands = listOf("Al-Qafila" to "القافلة", "Al-Waha" to "الواحة", "Lipton" to "ليبتون", "Sultan" to "سلطان", "Tayba" to "طيبة"),
            units = listOf("100g", "250g", "علبة 250غ", "500g", "25 أكياس")
        ),
        ProductTemplate(
            baseEn = "Table Mayonnaise & Sauces",
            baseAr = "مايونيز وصلصات المائدة",
            category = "Pantry",
            defaultPriceDzd = 175.0,
            brands = listOf("Lesieur" to "لوسيور", "Darna" to "دارنا", "Amora" to "أمورا", "Samouraï" to "ساموراي"),
            units = listOf("250g", "450g", "قارورة ضاغطة 450غ", "صلصة باربكيو 350g")
        ),
        ProductTemplate(
            baseEn = "Tuna In Vegetable Oil",
            baseAr = "تونة فاخرة بزيت المائدة",
            category = "Pantry",
            defaultPriceDzd = 190.0,
            brands = listOf("Maratun" to "ماراتون", "Saupiquet" to "سوبيكي", "Isabel" to "إيزابيل", "El Manar" to "المنار"),
            units = listOf("80g", "3x80g", "160g قطعة واحدة", "بالطماطم 160g")
        ),

        // Fresh Produce & Farm Goods (خضر وفواكه طازجة)
        ProductTemplate(
            baseEn = "Fresh Potatoes",
            baseAr = "بطاطا محلية طازجة",
            category = "Produce",
            defaultPriceDzd = 80.0,
            brands = listOf("Ain Defla Farm" to "مزارع عين الدفلى", "Mascara Farm" to "مزارع معسكر", "Oued Souf" to "وادي سوف"),
            units = listOf("1kg", "2kg", "كيس 5 كغ", "10kg")
        ),
        ProductTemplate(
            baseEn = "Fresh Red Tomatoes",
            baseAr = "طماطم حمراء طازجة",
            category = "Produce",
            defaultPriceDzd = 95.0,
            brands = listOf("Mitidja Plain" to "سهل متيجة", "Biskra Greenhouse" to "بيوت بسكرة", "Mostaganem" to "مستغانم"),
            units = listOf("1kg", "2kg", "صندوق 3 كغ")
        ),
        ProductTemplate(
            baseEn = "Dry & Green Onions",
            baseAr = "بصل جاف وأخضر",
            category = "Produce",
            defaultPriceDzd = 70.0,
            brands = listOf("Algerian Terroir" to "إنتاج وطني", "Mascara" to "معسكر", "El Oued" to "الوادي"),
            units = listOf("1kg", "2kg", "ربطة 3 كغ")
        ),
        ProductTemplate(
            baseEn = "Fresh Sweet Carrots",
            baseAr = "جزر حلو طازج",
            category = "Produce",
            defaultPriceDzd = 85.0,
            brands = listOf("Blida Terroir" to "مزارع البليدة", "Tipaza Farm" to "مزارع تيبازة"),
            units = listOf("1kg", "2kg", "كيس 2 كغ")
        ),
        ProductTemplate(
            baseEn = "Golden Delicious Apples",
            baseAr = "تفاح محلي ومستورد",
            category = "Produce",
            defaultPriceDzd = 280.0,
            brands = listOf("Batna Aurès" to "تفاح باتنة الأوراس", "Médéa Hills" to "تفاح المدية", "Imported" to "مستورد ممتاز"),
            units = listOf("1kg", "2kg", "سلة 1.5 كغ")
        ),
        ProductTemplate(
            baseEn = "Fresh Sweet Bananas",
            baseAr = "موز أصفر طازج وحلو",
            category = "Produce",
            defaultPriceDzd = 380.0,
            brands = listOf("Cavendish Select" to "كافنديش ممتاز", "Ecuador Fresh" to "إكوادور طازج", "Dole" to "دول"),
            units = listOf("1kg", "1.5kg", "2kg")
        ),
        ProductTemplate(
            baseEn = "Sweet Clementine & Oranges",
            baseAr = "برتقال وكليمنتين حلو",
            category = "Produce",
            defaultPriceDzd = 140.0,
            brands = listOf("Mitidja Citrus" to "حمضيات متيجة", "Boufarik" to "بوفاريك", "Mascara" to "معسكر"),
            units = listOf("1kg", "2kg", "شبكة 3 كغ")
        ),

        // Beverages & Juices (مشروبات ومياه)
        ProductTemplate(
            baseEn = "Mineral Natural Water",
            baseAr = "مياه معدنية طبيعية نقية",
            category = "Beverages",
            defaultPriceDzd = 35.0,
            brands = listOf("Ifri" to "إيفري", "Lalla Khedidja" to "لالة خديجة", "Guedila" to "قديلة", "Saida" to "سعيدة", "Mantara" to "منترة", "Toudja" to "توجة"),
            units = listOf("0.5L", "1.5L", "قارورة 1.5 لتر", "Pack 6x1.5L (حزمة)")
        ),
        ProductTemplate(
            baseEn = "Fruit Nectar & Juice",
            baseAr = "عصير فواكه طبيعي ومنعش",
            category = "Beverages",
            defaultPriceDzd = 160.0,
            brands = listOf("Rami" to "رامي", "Rouiba" to "رويبة", "N’gaous" to "نقاوس", "Ifri Fruit" to "إيفري فروت", "Djudjura" to "جرجرة"),
            units = listOf("Orange 1L", "برتقال 1 لتر", "Cocktail 1L", "كوكتيل 1 لتر", "Pêche 2L", "خوخ 2 لتر", "Pomme 1L", "تفاح 1 لتر")
        ),
        ProductTemplate(
            baseEn = "Carbonated Soft Drinks",
            baseAr = "مشروب غازي منعش ولذيذ",
            category = "Beverages",
            defaultPriceDzd = 120.0,
            brands = listOf("Hamoud Boualem" to "حمود بوعلام", "Selecto" to "سليكتو", "Coca-Cola" to "كوكا كولا", "Pepsi" to "بيبسي", "Fanta" to "فانتا"),
            units = listOf("Canette 33cl", "علبة 33 سل", "1L", "1.5L", "2L عائلي")
        ),

        // Butchery & Poultry (لحوم ودواجن وأسماك)
        ProductTemplate(
            baseEn = "Fresh Farm Chicken",
            baseAr = "دجاج بلدي كامل وطازج",
            category = "Butchery",
            defaultPriceDzd = 440.0,
            brands = listOf("Al-Djazair Poultry" to "دواجن الجزائر", "El-Baraka" to "مزارع البركة", "El-Fedjr" to "الفجر"),
            units = listOf("1kg", "1.5kg حبة كاملة", "2kg", "فخذ دجاج 1kg", "أجنحة 1kg")
        ),
        ProductTemplate(
            baseEn = "Fresh Turkey Escalope",
            baseAr = "إسكالوب ديك رومي طازج",
            category = "Butchery",
            defaultPriceDzd = 890.0,
            brands = listOf("Bellat" to "بيلط", "El-Djazair" to "دواجن الجزائر", "Boucherie Centrale" to "ملحمة النخبة"),
            units = listOf("500g شرائح", "1kg", "2kg")
        ),
        ProductTemplate(
            baseEn = "Ground Fresh Beef & Veal",
            baseAr = "لحم بقر وعجل مفروم طازج",
            category = "Butchery",
            defaultPriceDzd = 1950.0,
            brands = listOf("Boucherie Traditionnelle" to "قصابة جزائرية", "Viande Locale" to "عجل محلي طازج"),
            units = listOf("500g", "1kg", "2kg كفتة متبلة")
        ),
        ProductTemplate(
            baseEn = "Halal Mortadella & Pâté",
            baseAr = "كاشير ومورتاديلا حلال",
            category = "Butchery",
            defaultPriceDzd = 240.0,
            brands = listOf("Bellat" to "بيلط", "Koutoubia" to "الكتبية", "Tassili" to "طاسيلي", "Cosider" to "كوسيدار"),
            units = listOf("Pâté Boeuf 350g", "كاشير بقري", "Pâté Volaille 350g", "كاشير دجاج حار", "Olives 500g", "بالزيتون 500غ")
        ),

        // Bakery & Breakfast Biscuits (مخبوزات وبسكويت)
        ProductTemplate(
            baseEn = "Fresh Baguette & Bread",
            baseAr = "خبز باقيت وسميد طازج",
            category = "Bakery",
            defaultPriceDzd = 15.0,
            brands = listOf("Boulangerie Tradition" to "مخبزة تقليدية", "Le Pain Doré" to "الرغيف الذهبي"),
            units = listOf("1 Pièce", "حبة باقيت", "خبز سميد دائري", "خبز نخالة صحي")
        ),
        ProductTemplate(
            baseEn = "Breakfast Biscuits & Wafers",
            baseAr = "بسكويت الشاي والقوفرات",
            category = "Bakery",
            defaultPriceDzd = 65.0,
            brands = listOf("Bimo" to "بيمو", "Palmary" to "بالماري", "Tif" to "تيف", "Saida" to "سعيدة", "Lu" to "لو"),
            units = listOf("Bimo Chocolat 100g", "شوكولا 100غ", "Gaufrettes 120g", "قوفرات فانيليا", "Petit Beurre 200g", "كرتون عائلي")
        ),
        ProductTemplate(
            baseEn = "Chocolate Hazelnut Spread",
            baseAr = "شوكولاطة الطلاء بالبندق",
            category = "Bakery",
            defaultPriceDzd = 380.0,
            brands = listOf("El Mordjene" to "المرجان", "Maxon" to "ماكسون", "Nutella" to "نوتيلا", "Chocodad" to "شوكوداد"),
            units = listOf("350g", "700g علبة زجاجية", "1kg سطل")
        ),

        // Household & Cleaning (مواد التنظيف والمنزل)
        ProductTemplate(
            baseEn = "Laundry Detergent Powder",
            baseAr = "مسحوق غسيل الملابس التلقائي",
            category = "Household",
            defaultPriceDzd = 480.0,
            brands = listOf("Isis" to "إيزيس", "Omo" to "أومو", "Ariel" to "أريال", "Skip" to "سكيب", "Bonux" to "بونيكس"),
            units = listOf("1.5kg", "3kg كيس", "5kg", "سائل غسيل 3L")
        ),
        ProductTemplate(
            baseEn = "Dishwashing Liquid Soap",
            baseAr = "سائل غسيل الأواني المركز",
            category = "Household",
            defaultPriceDzd = 160.0,
            brands = listOf("Pril Isis" to "بريل إيزيس", "Test" to "تيست", "Maxis" to "ماكسيس", "Fairy" to "فيري"),
            units = listOf("Citron 750ml", "ليمون 750 مل", "1.25L", "3L عائلي")
        ),
        ProductTemplate(
            baseEn = "Bleach Disinfectant",
            baseAr = "ماء جافيل معقم ومبيض",
            category = "Household",
            defaultPriceDzd = 110.0,
            brands = listOf("Bref" to "بريف", "Ace" to "آس", "La Croix" to "لاكروى", "Djazair Net" to "جزائر نت"),
            units = listOf("1L", "2L", "5L بيدون")
        ),
        ProductTemplate(
            baseEn = "Bath & Hand Soap Bars",
            baseAr = "صابون اليدين والاستحمام",
            category = "Household",
            defaultPriceDzd = 75.0,
            brands = listOf("Palmolive" to "بالموليف", "Dove" to "دوف", "Duru" to "دورو", "Lux" to "لوكس", "Marseille" to "مرسيليا أصيل"),
            units = listOf("100g", "125g", "Lot de 4 قطعات")
        )
    )

    /**
     * Generates a deterministic list of 1,000 distinct Algerian grocery items with barcodes and realistic DZD prices.
     */
    fun generate1000Products(): List<ShoppingItem> {
        val result = mutableListOf<ShoppingItem>()
        var currentId = 1L
        var templateIdx = 0

        // Iterate until we reach 1,000 products
        while (result.size < 1000) {
            val template = templates[templateIdx % templates.size]
            for (brand in template.brands) {
                for (unit in template.units) {
                    if (result.size >= 1000) break

                    val barcodeNum = 6130000000000L + currentId
                    val barcode = barcodeNum.toString()

                    val nameEn = "${brand.first} ${template.baseEn} ($unit)"
                    val nameAr = "${template.baseAr} ${brand.second} ($unit)"

                    // Slight price variation based on brand and unit index
                    val priceMultiplier = 1.0 + ((currentId % 7) * 0.05) - 0.15
                    val finalPrice = (template.defaultPriceDzd * priceMultiplier).toInt().coerceAtLeast(15)

                    result.add(
                        ShoppingItem(
                            id = currentId,
                            name = nameEn,
                            nameAr = nameAr,
                            category = template.category,
                            quantity = 1,
                            unit = unit,
                            isChecked = false,
                            barcode = barcode,
                            estimatedPriceDzd = finalPrice.toDouble(),
                            isFrequent = (currentId % 25 == 0L)
                        )
                    )
                    currentId++
                }
                if (result.size >= 1000) break
            }
            templateIdx++
        }

        return result
    }

    /**
     * Generates market prices for each product in Algerian supermarkets.
     */
    fun generateMarketPricesForCatalog(products: List<ShoppingItem>): List<MarketPrice> {
        val stores = listOf(
            Triple("Uno Hypermarket", "هايبرماركت أونو", 1.0),
            Triple("Ardis Alger", "أرديس الجزائر", 1.03),
            Triple("Superette El Baraka", "سوبرماركت البركة", 0.98),
            Triple("Carrefour Alger", "كارفور الجزائر", 1.02),
            Triple("Superette En-Nour", "سوبرماركت النور", 0.99)
        )

        val prices = mutableListOf<MarketPrice>()
        for (product in products) {
            val basePrice = product.estimatedPriceDzd
            val barcode = product.barcode ?: continue

            // Pick 2 to 3 stores for each product
            val pickedStores = stores.shuffled().take(2)
            for (store in pickedStores) {
                val storePrice = (basePrice * store.third).toInt().toDouble().coerceAtLeast(15.0)
                prices.add(
                    MarketPrice(
                        barcode = barcode,
                        itemName = product.name,
                        itemNameAr = product.nameAr,
                        storeName = store.first,
                        storeNameAr = store.second,
                        priceDzd = storePrice,
                        isPromo = (product.id % 17 == 0L),
                        promoNote = if (product.id % 17 == 0L) "عرض خاص" else null,
                        verifiedCount = (1..15).random()
                    )
                )
            }
        }
        return prices
    }
}
