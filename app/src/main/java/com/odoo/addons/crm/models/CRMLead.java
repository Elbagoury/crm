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
 * Created on 13/1/15 10:07 AM
 */
package com.odoo.addons.crm.models;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.odoo.base.addons.res.ResCompany;
import com.odoo.base.addons.res.ResCountry;
import com.odoo.base.addons.res.ResCurrency;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import org.json.JSONArray;

public class CRMLead extends OModel {
    public static final String TAG = CRMLead.class.getSimpleName();
    public static final String AUTHORITY = "com.odoo.core.crm.provider.content.sync.crm_lead";
    private Context mContext;

    @Odoo.onChange(method = "partnerIdOnChange")
    OColumn partner_id = new OColumn("Customer", ResPartner.class,
            OColumn.RelationType.ManyToOne);
    OColumn name = new OColumn("Name", OVarchar.class).setSize(64)
            .setRequired();
    OColumn email_from = new OColumn("Email", OVarchar.class).setSize(128);
    OColumn street = new OColumn("Street", OText.class);
    OColumn street2 = new OColumn("Street2", OText.class);
    OColumn city = new OColumn("City", OVarchar.class).setSize(100);
    OColumn zip = new OColumn("Zip", OVarchar.class).setSize(20);
    OColumn phone = new OColumn("Phone", OVarchar.class).setSize(20);
    OColumn create_date = new OColumn("Creation Date", ODateTime.class);
    OColumn description = new OColumn("Internal Notes", OText.class);
    @Odoo.api.v7
    @Odoo.api.v8
    OColumn categ_ids = new OColumn("Tags", CRMCaseCateg.class,
            OColumn.RelationType.ManyToMany);
    @Odoo.api.v9alpha
    OColumn tag_ids = new OColumn("Tags", CRMCaseCateg.class,
            OColumn.RelationType.ManyToMany);
    OColumn contact_name = new OColumn("Contact Name", OVarchar.class);
    OColumn partner_name = new OColumn("Company Name", OVarchar.class);
    OColumn opt_out = new OColumn("Opt-Out", OBoolean.class);
    OColumn type = new OColumn("Type", OVarchar.class).setDefaultValue("lead");
    OColumn priority = new OColumn("Priority", OVarchar.class).setSize(10);
    OColumn date_open = new OColumn("Assigned", ODateTime.class);
    OColumn date_closed = new OColumn("Closed", ODateTime.class);
    OColumn stage_id = new OColumn("Stage", CRMCaseStage.class,
            OColumn.RelationType.ManyToOne);
    OColumn user_id = new OColumn("Salesperson", ResUsers.class,
            OColumn.RelationType.ManyToOne);
    OColumn referred = new OColumn("Referred By", OVarchar.class);
    OColumn company_id = new OColumn("Company", ResCompany.class,
            OColumn.RelationType.ManyToOne);
    OColumn country_id = new OColumn("Country", ResCountry.class,
            OColumn.RelationType.ManyToOne);
    OColumn company_currency = new OColumn("Company Currency",
            ResCurrency.class, OColumn.RelationType.ManyToOne);

    /**
     * Only used for type opportunity
     */

    OColumn probability = new OColumn("Success Rate (%)", OFloat.class).setSize(20);
    OColumn planned_revenue = new OColumn("Expected Revenue", OFloat.class).setSize(20);
    OColumn ref = new OColumn("Reference", OVarchar.class);
    OColumn ref2 = new OColumn("Reference 2", OVarchar.class);
    OColumn date_deadline = new OColumn("Expected Closing", ODate.class);
    OColumn date_action = new OColumn("Next Action Date", ODate.class);
    OColumn title_action = new OColumn("Next Action", OVarchar.class);
    OColumn planned_cost = new OColumn("Planned Cost", OFloat.class).setSize(20);

    /**
     * Extra functional fields
     */
    @Odoo.Functional(method = "getDisplayName", store = true, depends = {
            "partner_id", "contact_name", "partner_name"})
    OColumn display_name = new OColumn("Display Name", OVarchar.class)
            .setLocalColumn();
    @Odoo.Functional(method = "storeAssigneeName", store = true, depends = {"user_id"})
    OColumn assignee_name = new OColumn("Assignee", OVarchar.class).setSize(100)
            .setLocalColumn();
    @Odoo.Functional(method = "storeStageName", store = true, depends = {"stage_id"})
    OColumn stage_name = new OColumn("Stage name", OVarchar.class);
    OColumn data_type = new OColumn("Data type", OVarchar.class).setSize(34)
            .setLocalColumn().setDefaultValue("opportunity");
    OColumn is_done = new OColumn("Mark as Done", OInteger.class)
            .setLocalColumn().setDefaultValue("0");


    public CRMLead(Context context, OUser user) {
        super(context, "crm.lead", user);
        mContext = context;
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public ODataRow partnerIdOnChange(ODataRow row) {
        ODataRow rec = new ODataRow();
        String display_name = "";
        ResCountry country = new ResCountry(mContext, null);
        try {
            rec.put("partner_name", row.getString("name"));
            rec.put("partner_name", rec.getString("partner_name"));
            if (!row.getString("parent_id").equals("false")) {
                if (row.get("parent_id") instanceof JSONArray) {
                    JSONArray parent_id = new JSONArray(
                            row.getString("parent_id"));
                    rec.put("partner_name", parent_id.get(1));
                    display_name = parent_id.getString(1);
                } else {
                    ODataRow parent_id = row.getM2ORecord("parent_id").browse();
                    if (parent_id != null) {
                        rec.put("partner_name", parent_id.getString("name"));
                        display_name = parent_id.getString("name");
                    }
                }
                if (!TextUtils.isEmpty(display_name))
                    display_name += " (" + row.getString("name") + ")";
                else
                    display_name += row.getString("name");
            } else {
                display_name = row.getString("name");
            }
            Integer country_id = 0;
            if (!row.getString("country_id").equals("false")) {
                if (row.get("country_id") instanceof JSONArray) {
                    JSONArray country_data = new JSONArray(
                            row.getString("country_id"));
                    country_id = country.selectRowId(country_data.getInt(0));
                    if (country_id == null) {
                        country_id = 0;
                    }
                } else {
                    ODataRow country_data = row.getM2ORecord("country_id")
                            .browse();
                    if (country_data != null) {
                        country_id = country_data.getInt(OColumn.ROW_ID);
                    }
                }
                if (country_id != 0)
                    rec.put("country_id", country_id);
            }
            rec.put("display_name", display_name);
            rec.put("street", row.getString("street"));
            rec.put("street2", row.getString("street2"));
            rec.put("city", row.getString("city"));
            rec.put("zip", row.getString("zip"));
            rec.put("email_from", row.getString("email"));
            rec.put("phone", row.getString("phone"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rec;
    }

    public String getDisplayName(OValues row) {
        String name = "";
        try {
            if (!row.getString("partner_id").equals("false")) {
                JSONArray partner_id = new JSONArray(
                        row.getString("partner_id"));
                name = partner_id.getString(1);
            } else if (!row.getString("partner_name").equals("false")) {
                name = row.getString("partner_name");
            }
            if (!row.getString("contact_name").equals("false")) {
                name += (TextUtils.isEmpty(name)) ? row
                        .getString("contact_name") : " ("
                        + row.getString("contact_name") + ")";
            }
            if (TextUtils.isEmpty(name)) {
                name = "No Partner";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public String storeAssigneeName(OValues vals) {
        return (!vals.getString("user_id").equals("false")) ? "Me"
                : "Unassigned";
    }

    public String storeStageName(OValues values) {
        try {
            JSONArray stage_id = new JSONArray(values.getString("stage_id"));
            return stage_id.getString(1);
        } catch (Exception e) {

        }
        return "false";
    }

}