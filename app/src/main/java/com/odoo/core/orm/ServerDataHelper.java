/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 7/1/15 6:23 PM
 */
package com.odoo.core.orm;

import android.content.Context;

import com.odoo.App;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OdooFields;
import com.odoo.core.utils.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import odoo.ODomain;
import odoo.Odoo;

public class ServerDataHelper {
    public static final String TAG = ServerDataHelper.class.getSimpleName();
    private OModel mModel;
    private Context mContext;
    private Odoo mOdoo;
    private App mApp;

    public ServerDataHelper(Context context, OModel model) {
        mContext = context;
        mModel = model;
        mOdoo = OSyncAdapter.createOdooInstance(mContext, model.getUser());
        mApp = (App) mContext.getApplicationContext();
    }

    public List<ODataRow> searchRecords(OdooFields fields, ODomain domain, int limit) {
        List<ODataRow> items = new ArrayList<>();
        try {
            if (mApp.inNetwork()) {
                JSONObject result = mOdoo.search_read(mModel.getModelName(),
                        fields.get(), domain.get(), 0, limit, null, null);
                JSONArray records = result.getJSONArray("records");
                if (records.length() > 0) {
                    for (int i = 0; i < records.length(); i++) {
                        items.add(JSONUtils.toDataRow(records.getJSONObject(i)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}