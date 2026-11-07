package com.example.util

import com.example.data.model.MarketPrice

data class ProductVariant(
    val barcode: String,
    val itemName: String,
    val itemNameAr: String,
    val storeName: String,
    val storeNameAr: String,
    val priceDzd: Double,
    val allPrices: List<MarketPrice>
)

data class ProductGroup(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val category: String,
    val variants: List<ProductVariant>,
    val minPrice: Double,
    val maxPrice: Double,
    val defaultVariant: ProductVariant
)

object ProductGroupingUtil {

    data class FamilyInfo(
        val id: String,
        val nameAr: String,
        val nameEn: String,
        val category: String
    )

    fun extractFamilyInfo(name: String, nameAr: String): FamilyInfo {
        val t = "$name $nameAr".lowercase()
        return when {
            // Juice
            t.contains("عصير") || t.contains("juice") || t.contains("jus ") || t.endsWith("jus") || t.contains("nectar") ->
                FamilyInfo("juice", "عصير وفواكه معصورة", "Fruit Juice & Nectar", "Beverages")

            // Milk
            t.contains("حليب") || t.contains("lait") || t.contains("milk") ->
                FamilyInfo("milk", "حليب طازج ومعقم", "Milk & Dairy", "Dairy")

            // Cultured milk & Lben
            t.contains("رايب") || t.contains("لبن") || t.contains("lben") || t.contains("rayeb") ->
                FamilyInfo("lben", "لبن ورايب طبيعي", "Lben & Cultured Milk", "Dairy")

            // Yogurt
            t.contains("ياغورت") || t.contains("زبادي") || t.contains("yaourt") || t.contains("yogurt") || t.contains("دانون") ->
                FamilyInfo("yogurt", "زبادي وياغورت بالفواكه", "Yogurt & Desserts", "Dairy")

            // Cheese
            t.contains("جبن") || t.contains("فرماج") || t.contains("fromage") || t.contains("cheese") ||
            t.contains("كيري") || t.contains("تارتينو") || t.contains("بربر") || t.contains("شيدر") ||
            t.contains("موزاريلا") || t.contains("غرويير") || t.contains("camembert") || t.contains("لافاش") ->
                FamilyInfo("cheese", "أجبان متنوعة (مثلثات، مبشور، شيدر)", "Cheeses & Spreads", "Dairy")

            // Eggs
            t.contains("بيض") || t.contains("oeuf") || t.contains("egg") ->
                FamilyInfo("eggs", "بيض المائدة", "Fresh Eggs", "Dairy")

            // Cold cuts & Pâté
            t.contains("كاشير") || t.contains("باتي") || t.contains("cachir") || t.contains("pâté") || t.contains("pate") || t.contains("مرتديلا") || t.contains("mortadelle") ->
                FamilyInfo("cachir", "كاشير وباتي حلال", "Cold Cuts & Pâté", "Butchery")

            // Meat & Poultry
            t.contains("دجاج") || t.contains("poulet") || t.contains("chicken") ||
            t.contains("اسكالوب") || t.contains("إسكالوب") || t.contains("escalope") ||
            t.contains("لحم") || t.contains("viande") || t.contains("beef") ||
            t.contains("مرقاز") || t.contains("merguez") || t.contains("مفروم") ->
                FamilyInfo("meat", "لحوم ودواجن طازجة", "Meat & Fresh Poultry", "Butchery")

            // Tuna & Seafood
            t.contains("تونة") || t.contains("طون") || t.contains("thon") || t.contains("tuna") ||
            t.contains("سردين") || t.contains("sardine") || t.contains("سمك") || t.contains("poisson") ->
                FamilyInfo("tuna", "تونة وسردين مصبر", "Tuna & Seafood", "Pantry")

            // Oil
            t.contains("زيت") || t.contains("huile") || t.contains("oil") || t.contains("إيليو") || t.contains("عافية") ->
                FamilyInfo("oil", "زيت المائدة وزيت الزيتون", "Cooking & Olive Oil", "Pantry")

            // Margarine & Butter
            t.contains("مارغرين") || t.contains("زبدة") || t.contains("سمن") || t.contains("beurre") || t.contains("margarine") ||
            t.contains("لابيل") || t.contains("فلوريال") || t.contains("صول") ->
                FamilyInfo("butter", "زبدة ومارغرين نباتي", "Butter & Margarine", "Dairy")

            // Bread & Bakery
            t.contains("خبز") || t.contains("pain") || t.contains("bread") ||
            t.contains("باقيت") || t.contains("باكيط") || t.contains("baguette") ||
            t.contains("مطلوع") || t.contains("كسرة") || t.contains("مسمن") || t.contains("محاجب") ||
            t.contains("كرواسون") || t.contains("croissant") || t.contains("بريوش") || t.contains("brioche") ->
                FamilyInfo("bread", "خبز باقيت ومخبوزات", "Bread & Bakery", "Bakery")

            // Couscous & Traditional Pasta
            t.contains("كسكس") || t.contains("couscous") || t.contains("طعام") ||
            t.contains("بركوكش") || t.contains("تليتلي") || t.contains("رشتة") || t.contains("شخشوخة") ->
                FamilyInfo("couscous", "كسكس وعجائن تقليدية", "Couscous & Traditional", "Pantry")

            // Pasta & Spaghetti
            t.contains("مقارونة") || t.contains("معكرونة") || t.contains("ماكارونة") || t.contains("مقرونة") ||
            t.contains("سباغيتي") || t.contains("spaghetti") || t.contains("pasta") || t.contains("pâte") || t.contains("pates") || t.contains("macaroni") ->
                FamilyInfo("pasta", "معكرونة وسباغيتي", "Pasta & Spaghetti", "Pantry")

            // Tomato Paste & Sauces
            t.contains("طماطم مصبرة") || t.contains("طماطم علبة") || t.contains("concentré") ||
            t.contains("صلصة") || t.contains("sauce") || t.contains("مايونيز") || t.contains("mayonnaise") ->
                FamilyInfo("sauce", "طماطم مصبرة وصلصات", "Tomato Paste & Sauces", "Pantry")

            // Fresh Vegetables
            t.contains("بطاطا") || t.contains("potato") || t.contains("pomme de terre") ->
                FamilyInfo("potato", "بطاطا محلية", "Potatoes", "Produce")

            t.contains("طماطم طازجة") || (t.contains("طماطم") && !t.contains("مصبرة") && !t.contains("علبة")) ->
                FamilyInfo("tomato", "طماطم طازجة", "Fresh Tomatoes", "Produce")

            t.contains("بصل") || t.contains("oignon") || t.contains("onion") ->
                FamilyInfo("onion", "بصل أحمر وأبيض", "Onions", "Produce")

            t.contains("ثوم") || t.contains("ail") || t.contains("garlic") ->
                FamilyInfo("garlic", "ثوم محلي", "Garlic", "Produce")

            t.contains("فلفل") || t.contains("حار") || t.contains("poivron") || t.contains("piment") ->
                FamilyInfo("pepper", "فلفل حلو وحار", "Peppers & Chili", "Produce")

            t.contains("جزر") || t.contains("زرودية") || t.contains("carotte") || t.contains("carrot") ->
                FamilyInfo("carrot", "جزر طازج", "Carrots", "Produce")

            t.contains("خيار") || t.contains("concombre") || t.contains("cucumber") ->
                FamilyInfo("cucumber", "خيار مقرمش", "Cucumbers", "Produce")

            t.contains("كوسة") || t.contains("جريوات") || t.contains("قرعة") || t.contains("courgette") || t.contains("zucchini") ->
                FamilyInfo("zucchini", "كوسة وجريوات", "Zucchini", "Produce")

            t.contains("سلطة") || t.contains("خس") || t.contains("salade") || t.contains("lettuce") ->
                FamilyInfo("lettuce", "سلطة وخس طازج", "Lettuce & Greens", "Produce")

            // Fresh Fruits
            t.contains("تفاح") || t.contains("pomme") || t.contains("apple") ->
                FamilyInfo("apple", "تفاح طازج", "Apples", "Produce")

            t.contains("موز") || t.contains("بنان") || t.contains("banane") || t.contains("banana") ->
                FamilyInfo("banana", "موز حلو طازج", "Bananas", "Produce")

            t.contains("برتقال") || t.contains("تشينة") || t.contains("كليمنتين") || t.contains("orange") || t.contains("clementine") ->
                FamilyInfo("orange", "برتقال وكليمنتين", "Oranges & Citrus", "Produce")

            t.contains("ليمون") || t.contains("قارص") || t.contains("citron") || t.contains("lemon") ->
                FamilyInfo("lemon", "ليمون أصفر وأخضر", "Lemons & Limes", "Produce")

            t.contains("فراولة") || t.contains("فريز") || t.contains("fraise") || t.contains("strawberry") ->
                FamilyInfo("strawberry", "فراولة جزائرية", "Strawberries", "Produce")

            t.contains("دلاع") || t.contains("بطيخ") || t.contains("شمام") || t.contains("pastèque") || t.contains("melon") ->
                FamilyInfo("melon", "دلاع وبطيخ", "Watermelon & Melons", "Produce")

            t.contains("خوخ") || t.contains("مشمش") || t.contains("عنب") || t.contains("رمان") || t.contains("نكتارين") || t.contains("pêche") || t.contains("abricot") || t.contains("raisin") ->
                FamilyInfo("seasonal_fruits", "فواكه موسمية طازجة", "Seasonal Fruits", "Produce")

            t.contains("تمر") || t.contains("تمور") || t.contains("دقلة") || t.contains("date") ->
                FamilyInfo("dates", "تمور ودقلة نور", "Algerian Dates", "Produce")

            t.contains("لوبيا خضراء") || t.contains("جلبانة") || t.contains("شفلور") || t.contains("كرنب") || t.contains("سلق") || t.contains("معدنوس") || t.contains("دبشة") ->
                FamilyInfo("greens", "خضر وأعشاب خضراء", "Fresh Greens & Herbs", "Produce")

            // Flour & Semolina
            t.contains("فرينة") || t.contains("farine") || t.contains("flour") ->
                FamilyInfo("flour", "فرينة ودقيق ممتاز", "Flour", "Pantry")

            t.contains("سميد") || t.contains("semoule") || t.contains("semolina") ->
                FamilyInfo("semolina", "سميد قمح رفيع", "Semolina", "Pantry")

            // Sugar
            t.contains("سكر") || t.contains("sucre") || t.contains("sugar") ->
                FamilyInfo("sugar", "سكر أبيض ومحليات", "Sugar", "Pantry")

            // Coffee
            t.contains("قهوة") || t.contains("بن") || t.contains("café") || t.contains("cafe") || t.contains("coffee") || t.contains("نسكافيه") ->
                FamilyInfo("coffee", "قهوة وبن مطحون", "Coffee", "Beverages")

            // Tea & Hot Herbals
            t.contains("شاي") || t.contains("تاي") || t.contains("thé") || t.contains("tea") ||
            t.contains("نعناع") || t.contains("زنجبيل") || t.contains("زعتر") || t.contains("تيزانة") ||
            t.contains("بابونج") || t.contains("لويزة") || t.contains("يانسون") || t.contains("حبة حلاوة") || t.contains("إكليل") ->
                FamilyInfo("tea", "شاي وأعشاب وتيزانات", "Tea & Herbal Infusions", "HerbalDrinks")

            // Water
            t.contains("ماء") || t.contains("eau") || t.contains("water") || t.contains("معدنية") || t.contains("سعيدة") ->
                FamilyInfo("water", "مياه معدنية طبيعية", "Bottled Mineral Water", "Beverages")

            // Soft drinks & Sodas
            t.contains("غازوز") || t.contains("كولا") || t.contains("cola") || t.contains("soda") ||
            t.contains("فانتا") || t.contains("بيبسي") || t.contains("حمود") || t.contains("مشروب غازي") ->
                FamilyInfo("soda", "مشروبات غازية", "Soft Drinks & Sodas", "Beverages")

            // Biscuits, Chocolate & Sweets
            t.contains("شوكولا") || t.contains("chocolat") || t.contains("chocolate") ||
            t.contains("بسكويت") || t.contains("biscuit") || t.contains("غوفرات") || t.contains("gaufrette") ||
            t.contains("حلوة الترك") || t.contains("شامية") || t.contains("حلويات") || t.contains("بقلاوة") || t.contains("مقروط") ->
                FamilyInfo("sweets", "بسكويت وشوكولاتة وحلويات", "Biscuits & Confectionery", "SweetsNuts")

            // Nuts & Snacks
            t.contains("لوز") || t.contains("فستق") || t.contains("جوز") || t.contains("كاجو") ||
            t.contains("كاوكاو") || t.contains("بندق") || t.contains("مكسرات") || t.contains("amande") ||
            t.contains("pistache") || t.contains("noisette") || t.contains("cacahuète") || t.contains("noix") ->
                FamilyInfo("nuts", "مكسرات ومسليات فاخرة", "Nuts & Snacks", "SweetsNuts")

            // Dried Legumes
            t.contains("عدس") || t.contains("حمص") || t.contains("لوبيا بيضاء") || t.contains("فول") ||
            t.contains("lentille") || t.contains("pois chiche") || t.contains("haricot") ->
                FamilyInfo("legumes", "بقوليات وحبوب جافة", "Dried Legumes & Pulses", "Pantry")

            // Household & Cleaning
            t.contains("غسيل") || t.contains("جافيل") || t.contains("أرييل") || t.contains("إيزيس") ||
            t.contains("صابون") || t.contains("شامبو") || t.contains("معجون أسنان") || t.contains("أواني") ||
            t.contains("lessive") || t.contains("savon") || t.contains("shampoo") ->
                FamilyInfo("cleaning", "منظفات ومواد العناية المنزلية", "Cleaning & Hygiene", "Household")

            // Default fallback based on clean title
            else -> {
                val clean = nameAr.split("-")[0].split("(")[0].trim()
                val id = clean.replace(" ", "_")
                FamilyInfo(id, clean, name.split(" ")[0], "Pantry")
            }
        }
    }

    /**
     * Groups raw MarketPrices into structured ProductGroups with variants and price ranges.
     */
    fun groupMarketPrices(prices: List<MarketPrice>): List<ProductGroup> {
        // 1. Group individual store prices by distinct barcode
        val byBarcode = prices.groupBy { it.barcode }

        val variants = byBarcode.map { (barcode, storePrices) ->
            val first = storePrices.first()
            val minPrice = storePrices.minOfOrNull { it.priceDzd } ?: first.priceDzd
            ProductVariant(
                barcode = barcode,
                itemName = first.itemName,
                itemNameAr = first.itemNameAr,
                storeName = first.storeName,
                storeNameAr = first.storeNameAr,
                priceDzd = minPrice,
                allPrices = storePrices
            )
        }

        // 2. Group variants by high-level product family
        val byFamily = variants.groupBy { variant ->
            extractFamilyInfo(variant.itemName, variant.itemNameAr)
        }

        return byFamily.map { (family, groupVariants) ->
            val sortedVariants = groupVariants.sortedBy { it.priceDzd }
            val minP = sortedVariants.firstOrNull()?.priceDzd ?: 0.0
            val maxP = sortedVariants.maxOfOrNull { it.priceDzd } ?: minP
            val defaultVar = sortedVariants.first()

            ProductGroup(
                id = family.id,
                nameAr = family.nameAr,
                nameEn = family.nameEn,
                category = family.category,
                variants = sortedVariants,
                minPrice = minP,
                maxPrice = maxP,
                defaultVariant = defaultVar
            )
        }.sortedByDescending { it.variants.size }
    }
}
