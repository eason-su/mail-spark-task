package cdlm.job2;

import com.alibaba.fastjson.JSONArray;
import com.chongdianleme.job.SQLHelper;

import java.io.Serializable;

public class ClassDemo2 implements Serializable {

    private String kcId;
    private String kcName;
    private float price;
    private boolean isSale;

    public static boolean isOnSale(String kcId) {
        boolean isSale = false;
        String sql = "SELECT issale FROM ods_kc_dim_product WHERE kcid=\"" + kcId + "\"";
        JSONArray jsonArray = SQLHelper.query(sql);
        if (jsonArray.size() > 0) {
            com.alibaba.fastjson.JSONObject jb = jsonArray.getJSONObject(0);
            isSale = Boolean.parseBoolean(jb.get("issale").toString());
        }
        return isSale;
    }

    public String getKcId() {
        return kcId;
    }

    public void setKcId(String kcId) {
        this.kcId = kcId;
    }

    public String getKcName() {
        return kcName;
    }

    public void setKcName(String kcName) {
        this.kcName = kcName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isSale() {
        return isSale;
    }

    public void setSale(boolean sale) {
        isSale = sale;
    }
}
