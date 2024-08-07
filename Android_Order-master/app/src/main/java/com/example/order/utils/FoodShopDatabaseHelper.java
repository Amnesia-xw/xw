package com.example.order.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.order.bean.FoodBean;
import com.example.order.bean.ShopBean;


import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

// TODO
public class FoodShopDatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称
    public static final String DATABASE_NAME = "food_shop.db";
    // 数据库版本
    public static final int DATABASE_VERSION = 1;

    // "shop"表的常量定义
    public static final String TABLE_SHOP = "shop_tab";
    public static final String COLUMN_SHOP_ID = "shopId";
    public static final String COLUMN_SHOP_NAME = "shopName";
    public static final String COLUMN_SHOP_SALE_NUM = "saleNum";
    public static final String COLUMN_SHOP_OFFER_PRICE = "offerPrice";
    public static final String COLUMN_SHOP_DISTRIBUTION_COST = "distributionCost";
    public static final String COLUMN_SHOP_FEATURE = "feature";
    public static final String COLUMN_SHOP_TIME = "time";
    public static final String COLUMN_SHOP_BANNER = "banner";
    public static final String COLUMN_SHOP_PIC = "shopPic";
    public static final String COLUMN_SHOP_NOTICE = "shopNotice";

    // "food"表的常量定义
    public static final String TABLE_FOOD = "food_tab";
    public static final String COLUMN_FOOD_UID = "foodUid";
    public static final String COLUMN_FOOD_ID = "foodId";
    public static final String COLUMN_FOOD_SHOP_ID = "foodShopId";
    public static final String COLUMN_FOOD_NAME = "foodName";
    public static final String COLUMN_FOOD_POPULARITY = "popularity";
    public static final String COLUMN_FOOD_SALE_NUM = "saleNum";
    public static final String COLUMN_FOOD_PRICE = "price";
    public static final String COLUMN_FOOD_COUNT = "count";
    public static final String COLUMN_FOOD_PIC = "foodPic";

    public static final String TABLE_SHOPPING_CART = "shopping_cart_tab";
    public static final String COLUMN_10000SHOP_FOOD_UID = "column10000ShopFoodUid";
    public static final String COLUMN_FOOD_COUNT_IN_CART = "columnFoodCountInCart";

    public FoodShopDatabaseHelper(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        // 创建 "shop" 表的 SQL 语句
        final String CREATE_TABLE_SHOP = "CREATE TABLE IF NOT EXISTS " + TABLE_SHOP + "("
                + COLUMN_SHOP_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SHOP_NAME + " TEXT,"
                + COLUMN_SHOP_SALE_NUM + " INTEGER,"
                + COLUMN_SHOP_OFFER_PRICE + " TEXT,"  // BigDecimal 在 SQLite 中以 TEXT 类型存储
                + COLUMN_SHOP_DISTRIBUTION_COST + " TEXT,"  // BigDecimal 在 SQLite 中以 TEXT 类型存储
                + COLUMN_SHOP_FEATURE + " TEXT,"
                + COLUMN_SHOP_TIME + " TEXT,"
                + COLUMN_SHOP_BANNER + " TEXT,"
                + COLUMN_SHOP_PIC + " TEXT,"
                + COLUMN_SHOP_NOTICE + " TEXT"
                + ")";

        // 创建 "food" 表的 SQL 语句
        final String CREATE_TABLE_FOOD = "CREATE TABLE IF NOT EXISTS " + TABLE_FOOD + "("
                + COLUMN_FOOD_UID + " INTEGER PRIMARY KEY AUTOINCREMENT," // 数据库内部使用的自增主键唯一ID
                + COLUMN_FOOD_SHOP_ID + " INTEGER," // 食品所属的店铺ID
                + COLUMN_FOOD_ID + " INTEGER,"
                + COLUMN_FOOD_NAME + " TEXT,"
                + COLUMN_FOOD_POPULARITY + " TEXT,"
                + COLUMN_FOOD_SALE_NUM + " TEXT,"  // 在这里假设销售数量是以字符串形式存储
                + COLUMN_FOOD_PRICE + " TEXT,"  // BigDecimal 在 SQLite 中以 TEXT 类型存储
                + COLUMN_FOOD_COUNT + " INTEGER,"
                + COLUMN_FOOD_PIC + " TEXT" + ")";

        final String CREATE_TABLE_SHOPPING_CART = "CREATE TABLE IF NOT EXISTS " + TABLE_SHOPPING_CART + "("
                + COLUMN_10000SHOP_FOOD_UID + " INTEGER PRIMARY KEY," // 数值等于 COLUMN_SHOP_ID 乘以 10000 加上 COLUMN_FOOD_UID，使得食品的编号唯一
                + COLUMN_FOOD_COUNT_IN_CART + " INTEGER" + ")"; // 购物车内此食品的数量

        // 执行 SQL 语句来创建表
        db.execSQL(CREATE_TABLE_SHOP);
        db.execSQL(CREATE_TABLE_FOOD);
        db.execSQL(CREATE_TABLE_SHOPPING_CART);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // not implemented
    }

    public void dropShopAndFoodData() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        onCreate(db);
        db.close();
    }

    // 添加 ShopBean 到数据库
    public void saveShopAndFood(@NonNull List<ShopBean> shopBeanList) {
        // 获取数据库写入权限
        SQLiteDatabase db = this.getWritableDatabase();
        for (ShopBean shopBean : shopBeanList) {
            db.insert(TABLE_SHOP, null, getShopBeanContentValues(shopBean));
            for (FoodBean foodBean : shopBean.getFoodList()) {
                db.insert(TABLE_FOOD, null, getFoodBeanContentValues(shopBean.getId(), foodBean));
            }
        }
        db.close();
    }

    public void saveShoppingCart(@NonNull ShopBean shopBean) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (FoodBean foodBean : shopBean.getFoodList()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_10000SHOP_FOOD_UID, shopBean.getId() * 10000 + foodBean.getFoodId());
            values.put(COLUMN_FOOD_COUNT_IN_CART, foodBean.getCount());
            db.insertWithOnConflict(TABLE_SHOPPING_CART, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    @SuppressLint("Range")
    public int getFoodCountInShoppingCart(int shopId, @NonNull FoodBean foodBean) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOPPING_CART, null, COLUMN_10000SHOP_FOOD_UID + "=?", new String[]{String.valueOf(shopId * 10000 + foodBean.getFoodId())}, null, null, null);
        int result= cursor.moveToFirst() ? cursor.getInt(cursor.getColumnIndex(COLUMN_FOOD_COUNT_IN_CART)) : 0;
        cursor.close();
        db.close();
        return result;
    }


    @NonNull
    private static ContentValues getFoodBeanContentValues(int shopId, @NonNull FoodBean foodBean) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOOD_SHOP_ID, shopId);
        values.put(COLUMN_FOOD_ID, foodBean.getFoodId());
        values.put(COLUMN_FOOD_NAME, foodBean.getFoodName());
        values.put(COLUMN_FOOD_SALE_NUM, foodBean.getSaleNum());
        values.put(COLUMN_FOOD_PRICE, foodBean.getPrice().toString()); // BigDecimal 转为 String
        values.put(COLUMN_FOOD_COUNT, foodBean.getCount());
        values.put(COLUMN_FOOD_PIC, foodBean.getFoodPic());
        return values;
    }

    @NonNull
    private static ContentValues getShopBeanContentValues(@NonNull ShopBean shopBean) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_ID, shopBean.getId());
        values.put(COLUMN_SHOP_NAME, shopBean.getShopName());
        values.put(COLUMN_SHOP_SALE_NUM, shopBean.getSaleNum());
        values.put(COLUMN_SHOP_OFFER_PRICE, shopBean.getOfferPrice().toString()); // BigDecimal 转为 String
        values.put(COLUMN_SHOP_DISTRIBUTION_COST, shopBean.getDistributionCost().toString()); // BigDecimal 转为 String
        values.put(COLUMN_SHOP_TIME, shopBean.getTime());
        values.put(COLUMN_SHOP_PIC, shopBean.getShopPic());
        values.put(COLUMN_SHOP_NOTICE, shopBean.getShopNotice());
        return values;
    }

    // 从数据库读取所有 ShopBean 数据
    @SuppressLint("Range")
    public List<ShopBean> getShopAndFood() {
        List<ShopBean> shopList = new LinkedList<>();
        // 获取数据库读取权限
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询 "shop" 表的所有数据
        Cursor cursor1 = db.query(TABLE_SHOP, null, null, null, null, null, null);

        // 遍历查询结果
        if (cursor1.moveToFirst()) {
            do {
                ShopBean shopBean = new ShopBean();
                shopBean.setId(cursor1.getInt(cursor1.getColumnIndex(COLUMN_SHOP_ID)));
                shopBean.setShopName(cursor1.getString(cursor1.getColumnIndex(COLUMN_SHOP_NAME)));
                shopBean.setSaleNum(cursor1.getInt(cursor1.getColumnIndex(COLUMN_SHOP_SALE_NUM)));
                shopBean.setOfferPrice(new BigDecimal(cursor1.getString(cursor1.getColumnIndex(COLUMN_SHOP_OFFER_PRICE))));
                shopBean.setDistributionCost(new BigDecimal(cursor1.getString(cursor1.getColumnIndex(COLUMN_SHOP_DISTRIBUTION_COST))));
                shopBean.setShopPic(cursor1.getString(cursor1.getColumnIndex(COLUMN_SHOP_PIC)));
                shopBean.setShopNotice(cursor1.getString(cursor1.getColumnIndex(COLUMN_SHOP_NOTICE)));
                List<FoodBean> foodList = new LinkedList<>();
                Cursor cursor2 = db.query(TABLE_FOOD, null, COLUMN_FOOD_SHOP_ID + "=?", new String[]{String.valueOf(shopBean.getId())}, null, null, null);
                if (cursor2.moveToFirst()) {
                    do {
                        FoodBean food = new FoodBean();
                        food.setFoodId(cursor2.getInt(cursor2.getColumnIndex(COLUMN_FOOD_ID)));
                        food.setFoodName(cursor2.getString(cursor2.getColumnIndex(COLUMN_FOOD_NAME)));
                        food.setPrice(new BigDecimal(cursor2.getString(cursor2.getColumnIndex(COLUMN_FOOD_PRICE))));
                        food.setCount(cursor2.getInt(cursor2.getColumnIndex(COLUMN_FOOD_COUNT)));
                        food.setFoodPic(cursor2.getString(cursor2.getColumnIndex(COLUMN_FOOD_PIC)));
                        foodList.add(food);
                    } while (cursor2.moveToNext());
                }
                shopBean.setFoodList(foodList);
                shopList.add(shopBean);
                cursor2.close();
            } while (cursor1.moveToNext());
            // 关闭 Cursor 和数据库连接
            cursor1.close();
            db.close();
        }
        return shopList;
    }
}
