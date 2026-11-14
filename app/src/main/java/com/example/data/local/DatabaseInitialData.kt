package com.example.data.local

import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import com.example.data.model.ShoppingItem

object DatabaseInitialData {

    val initialShoppingItems = listOf(
        ShoppingItem(
            id = 1,
            name = "Candia Viva Milk 1L",
            nameAr = "حليب كانديا فيفا 1 لتر",
            category = "Dairy",
            quantity = 2,
            unit = "L",
            isChecked = false,
            barcode = "6130001000123",
            estimatedPriceDzd = 125.0,
            isFrequent = true
        ),
        ShoppingItem(
            id = 2,
            name = "Elio Cooking Oil 5L",
            nameAr = "زيت المائدة إيليو 5 لتر",
            category = "Pantry",
            quantity = 1,
            unit = "pcs",
            isChecked = false,
            barcode = "6130002000456",
            estimatedPriceDzd = 620.0,
            isFrequent = true
        ),
        ShoppingItem(
            id = 3,
            name = "Famico Ground Coffee 250g",
            nameAr = "قهوة فاميكو مطحونة 250 غرام",
            category = "Pantry",
            quantity = 2,
            unit = "pack",
            isChecked = true,
            barcode = "6130003000789",
            estimatedPriceDzd = 310.0,
            isFrequent = true
        ),
        ShoppingItem(
            id = 4,
            name = "Fresh Farm Eggs (30 pcs)",
            nameAr = "بيض مزارع طازج (طبق 30 حبة)",
            category = "Dairy",
            quantity = 1,
            unit = "pack",
            isChecked = false,
            barcode = "6130010000777",
            estimatedPriceDzd = 540.0,
            isFrequent = true
        ),
        ShoppingItem(
            id = 5,
            name = "Sim Couscous Moyenne 1kg",
            nameAr = "كسكس سيم متوسط 1 كغ",
            category = "Pantry",
            quantity = 1,
            unit = "kg",
            isChecked = false,
            barcode = "6130005000222",
            estimatedPriceDzd = 160.0,
            isFrequent = true
        ),
        ShoppingItem(
            id = 6,
            name = "Berbère Cheese 24 Portions",
            nameAr = "جبن مثلثات بربر 24 قطعة",
            category = "Dairy",
            quantity = 1,
            unit = "pack",
            isChecked = false,
            barcode = "6130006000333",
            estimatedPriceDzd = 380.0,
            isFrequent = false
        ),
        ShoppingItem(
            id = 7,
            name = "Ifri Mineral Water 6x1.5L",
            nameAr = "مياه معدنية إيفري 6×1.5 لتر",
            category = "Beverages",
            quantity = 1,
            unit = "pack",
            isChecked = false,
            barcode = "6130009000666",
            estimatedPriceDzd = 200.0,
            isFrequent = true
        )
    )

    val frequentPresets = listOf(
        ShoppingItem(name = "Baguette Bread", nameAr = "خبز باقيت طازج", category = "Bakery", quantity = 3, unit = "pcs", estimatedPriceDzd = 15.0, isFrequent = true),
        ShoppingItem(name = "Tomatoes Fresh 1kg", nameAr = "طماطم طازجة 1 كغ", category = "Produce", quantity = 1, unit = "kg", estimatedPriceDzd = 110.0, isFrequent = true),
        ShoppingItem(name = "Potatoes 2kg", nameAr = "بطاطا محلية 2 كغ", category = "Produce", quantity = 2, unit = "kg", estimatedPriceDzd = 85.0, isFrequent = true),
        ShoppingItem(name = "Cevital White Sugar 1kg", nameAr = "سكر سيفيتال 1 كغ", category = "Pantry", quantity = 1, unit = "kg", barcode = "6130011000888", estimatedPriceDzd = 95.0, isFrequent = true),
        ShoppingItem(name = "Rami Orange Juice 2L", nameAr = "عصير رامي برتقال 2 لتر", category = "Beverages", quantity = 1, unit = "pcs", barcode = "6130008000555", estimatedPriceDzd = 190.0, isFrequent = true),
        ShoppingItem(name = "Fresh Chicken 1kg", nameAr = "دجاج بلدي طازج 1 كغ", category = "Butchery", quantity = 1, unit = "kg", barcode = "6130012000999", estimatedPriceDzd = 440.0, isFrequent = true),
        ShoppingItem(name = "Isis Detergent 3kg", nameAr = "مسحوق غسيل إيزيس 3 كغ", category = "Household", quantity = 1, unit = "pcs", barcode = "6130007000444", estimatedPriceDzd = 750.0, isFrequent = true),
        ShoppingItem(name = "Cab Tomato Paste 800g", nameAr = "طماطم مصبرة كاب 800 غرام", category = "Pantry", quantity = 1, unit = "pcs", barcode = "6130004000111", estimatedPriceDzd = 250.0, isFrequent = true)
    )

    val initialMarketPrices = listOf(
        // Candia Viva Milk 1L - 6130001000123
        MarketPrice(barcode = "6130001000123", itemName = "Candia Viva Milk 1L", itemNameAr = "حليب كانديا فيفا 1 لتر", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 120.0, isPromo = true, promoNote = "Promo Lot"),
        MarketPrice(barcode = "6130001000123", itemName = "Candia Viva Milk 1L", itemNameAr = "حليب كانديا فيفا 1 لتر", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 125.0),
        MarketPrice(barcode = "6130001000123", itemName = "Candia Viva Milk 1L", itemNameAr = "حليب كانديا فيفا 1 لتر", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 130.0),
        MarketPrice(barcode = "6130001000123", itemName = "Candia Viva Milk 1L", itemNameAr = "حليب كانديا فيفا 1 لتر", storeName = "Carrefour Alger", storeNameAr = "كارفور الجزائر", priceDzd = 128.0),

        // Elio Oil 5L - 6130002000456
        MarketPrice(barcode = "6130002000456", itemName = "Elio Cooking Oil 5L", itemNameAr = "زيت المائدة إيليو 5 لتر", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 620.0, isPromo = true, promoNote = "Best Value"),
        MarketPrice(barcode = "6130002000456", itemName = "Elio Cooking Oil 5L", itemNameAr = "زيت المائدة إيليو 5 لتر", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 640.0),
        MarketPrice(barcode = "6130002000456", itemName = "Elio Cooking Oil 5L", itemNameAr = "زيت المائدة إيليو 5 لتر", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 650.0),
        MarketPrice(barcode = "6130002000456", itemName = "Elio Cooking Oil 5L", itemNameAr = "زيت المائدة إيليو 5 لتر", storeName = "Carrefour Alger", storeNameAr = "كارفور الجزائر", priceDzd = 645.0),

        // Famico Coffee 250g - 6130003000789
        MarketPrice(barcode = "6130003000789", itemName = "Famico Ground Coffee 250g", itemNameAr = "قهوة فاميكو مطحونة 250 غرام", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 310.0, isPromo = true),
        MarketPrice(barcode = "6130003000789", itemName = "Famico Ground Coffee 250g", itemNameAr = "قهوة فاميكو مطحونة 250 غرام", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 330.0),
        MarketPrice(barcode = "6130003000789", itemName = "Famico Ground Coffee 250g", itemNameAr = "قهوة فاميكو مطحونة 250 غرام", storeName = "Carrefour Alger", storeNameAr = "كارفور الجزائر", priceDzd = 340.0),
        MarketPrice(barcode = "6130003000789", itemName = "Famico Ground Coffee 250g", itemNameAr = "قهوة فاميكو مطحونة 250 غرام", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 325.0),

        // Cab Tomato Paste 800g - 6130004000111
        MarketPrice(barcode = "6130004000111", itemName = "Cab Tomato Paste 800g", itemNameAr = "طماطم مصبرة كاب 800 غرام", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 250.0),
        MarketPrice(barcode = "6130004000111", itemName = "Cab Tomato Paste 800g", itemNameAr = "طماطم مصبرة كاب 800 غرام", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 260.0),
        MarketPrice(barcode = "6130004000111", itemName = "Cab Tomato Paste 800g", itemNameAr = "طماطم مصبرة كاب 800 غرام", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 275.0),

        // Sim Couscous 1kg - 6130005000222
        MarketPrice(barcode = "6130005000222", itemName = "Sim Couscous Moyenne 1kg", itemNameAr = "كسكس سيم متوسط 1 كغ", storeName = "Superette En-Nour", storeNameAr = "سوبرماركت النور", priceDzd = 160.0),
        MarketPrice(barcode = "6130005000222", itemName = "Sim Couscous Moyenne 1kg", itemNameAr = "كسكس سيم متوسط 1 كغ", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 165.0),
        MarketPrice(barcode = "6130005000222", itemName = "Sim Couscous Moyenne 1kg", itemNameAr = "كسكس سيم متوسط 1 كغ", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 170.0),

        // Berbere Cheese 24p - 6130006000333
        MarketPrice(barcode = "6130006000333", itemName = "Berbère Cheese 24 Portions", itemNameAr = "جبن مثلثات بربر 24 قطعة", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 380.0),
        MarketPrice(barcode = "6130006000333", itemName = "Berbère Cheese 24 Portions", itemNameAr = "جبن مثلثات بربر 24 قطعة", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 390.0),
        MarketPrice(barcode = "6130006000333", itemName = "Berbère Cheese 24 Portions", itemNameAr = "جبن مثلثات بربر 24 قطعة", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 410.0),

        // Isis Detergent 3kg - 6130007000444
        MarketPrice(barcode = "6130007000444", itemName = "Isis Detergent 3kg", itemNameAr = "مسحوق غسيل إيزيس 3 كغ", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 750.0, isPromo = true),
        MarketPrice(barcode = "6130007000444", itemName = "Isis Detergent 3kg", itemNameAr = "مسحوق غسيل إيزيس 3 كغ", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 780.0),
        MarketPrice(barcode = "6130007000444", itemName = "Isis Detergent 3kg", itemNameAr = "مسحوق غسيل إيزيس 3 كغ", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 790.0),

        // Rami Juice 2L - 6130008000555
        MarketPrice(barcode = "6130008000555", itemName = "Rami Orange Juice 2L", itemNameAr = "عصير رامي برتقال 2 لتر", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 190.0),
        MarketPrice(barcode = "6130008000555", itemName = "Rami Orange Juice 2L", itemNameAr = "عصير رامي برتقال 2 لتر", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 195.0),
        MarketPrice(barcode = "6130008000555", itemName = "Rami Orange Juice 2L", itemNameAr = "عصير رامي برتقال 2 لتر", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 200.0),

        // Ifri Water 6x1.5L - 6130009000666
        MarketPrice(barcode = "6130009000666", itemName = "Ifri Mineral Water 6x1.5L", itemNameAr = "مياه معدنية إيفري 6×1.5 لتر", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 200.0),
        MarketPrice(barcode = "6130009000666", itemName = "Ifri Mineral Water 6x1.5L", itemNameAr = "مياه معدنية إيفري 6×1.5 لتر", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 210.0),
        MarketPrice(barcode = "6130009000666", itemName = "Ifri Mineral Water 6x1.5L", itemNameAr = "مياه معدنية إيفري 6×1.5 لتر", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 215.0),

        // Eggs 30pcs - 6130010000777
        MarketPrice(barcode = "6130010000777", itemName = "Fresh Farm Eggs (30 pcs)", itemNameAr = "بيض مزارع طازج (طبق 30 حبة)", storeName = "Superette En-Nour", storeNameAr = "سوبرماركت النور", priceDzd = 540.0, isPromo = true),
        MarketPrice(barcode = "6130010000777", itemName = "Fresh Farm Eggs (30 pcs)", itemNameAr = "بيض مزارع طازج (طبق 30 حبة)", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 580.0),
        MarketPrice(barcode = "6130010000777", itemName = "Fresh Farm Eggs (30 pcs)", itemNameAr = "بيض مزارع طازج (طبق 30 حبة)", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 600.0),

        // Cevital Sugar 1kg - 6130011000888
        MarketPrice(barcode = "6130011000888", itemName = "Cevital White Sugar 1kg", itemNameAr = "سكر سيفيتال 1 كغ", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 95.0),
        MarketPrice(barcode = "6130011000888", itemName = "Cevital White Sugar 1kg", itemNameAr = "سكر سيفيتال 1 كغ", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 95.0),
        MarketPrice(barcode = "6130011000888", itemName = "Cevital White Sugar 1kg", itemNameAr = "سكر سيفيتال 1 كغ", storeName = "Superette El Baraka", storeNameAr = "سوبرماركت البركة", priceDzd = 95.0),

        // Fresh Chicken 1kg - 6130012000999
        MarketPrice(barcode = "6130012000999", itemName = "Fresh Chicken 1kg", itemNameAr = "دجاج بلدي طازج 1 كغ", storeName = "Superette En-Nour", storeNameAr = "سوبرماركت النور", priceDzd = 440.0),
        MarketPrice(barcode = "6130012000999", itemName = "Fresh Chicken 1kg", itemNameAr = "دجاج بلدي طازج 1 كغ", storeName = "Uno Hypermarket", storeNameAr = "هايبرماركت أونو", priceDzd = 470.0),
        MarketPrice(barcode = "6130012000999", itemName = "Fresh Chicken 1kg", itemNameAr = "دجاج بلدي طازج 1 كغ", storeName = "Ardis Alger", storeNameAr = "أرديس الجزائر", priceDzd = 490.0)
    )

    val initialPriceHistories = listOf(
        PriceHistory(barcode = "6130001000123", storeName = "Uno Hypermarket", priceDzd = 135.0, dateLabel = "Aug"),
        PriceHistory(barcode = "6130001000123", storeName = "Uno Hypermarket", priceDzd = 130.0, dateLabel = "Sep"),
        PriceHistory(barcode = "6130001000123", storeName = "Uno Hypermarket", priceDzd = 125.0, dateLabel = "Oct"),

        PriceHistory(barcode = "6130002000456", storeName = "Uno Hypermarket", priceDzd = 650.0, dateLabel = "Aug"),
        PriceHistory(barcode = "6130002000456", storeName = "Uno Hypermarket", priceDzd = 635.0, dateLabel = "Sep"),
        PriceHistory(barcode = "6130002000456", storeName = "Uno Hypermarket", priceDzd = 620.0, dateLabel = "Oct")
    )

    val initialSpendingRecords = listOf(
        com.example.data.model.SpendingRecord(
            timestamp = System.currentTimeMillis() - 4 * 86400000L,
            monthKey = "2026-11",
            dateStr = "04/11/2026",
            totalSpentDzd = 4850.0,
            itemsCount = 9,
            storeName = "Uno Hypermarket",
            storeNameAr = "هايبرماركت أونو",
            itemsSummary = "حليب، زيت عافية، سكر سيفيتال، بيض مزارع طازج",
            topCategory = "Dairy"
        ),
        com.example.data.model.SpendingRecord(
            timestamp = System.currentTimeMillis() - 2 * 86400000L,
            monthKey = "2026-11",
            dateStr = "06/11/2026",
            totalSpentDzd = 6240.0,
            itemsCount = 13,
            storeName = "Ardis Alger",
            storeNameAr = "أرديس الجزائر",
            itemsSummary = "دجاج بلدي طازج، مسحوق غسيل إيزيس، عصير رامي",
            topCategory = "Butchery"
        ),
        com.example.data.model.SpendingRecord(
            timestamp = System.currentTimeMillis() - 12 * 86400000L,
            monthKey = "2026-10",
            dateStr = "27/10/2026",
            totalSpentDzd = 5120.0,
            itemsCount = 11,
            storeName = "Superette El Baraka",
            storeNameAr = "سوبرماركت البركة",
            itemsSummary = "سميد، طماطم مصبرة، تونة بالزيت، مياه إيفري",
            topCategory = "Pantry"
        )
    )
}
