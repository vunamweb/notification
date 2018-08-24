/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package com.csc.integralpos.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.csc.dip.jvpms.runtime.base.VpmsLoadFailedException;
import com.csc.integralpos.R;
import com.csc.integralpos.common.Constants;
import com.csc.integralpos.module.IposModuleBuilder;
import com.csc.integralpos.module.services.IIposModuleService;
import com.csc.integralpos.module.services.ServiceConstants;
import com.csc.integralpos.module.services.account.AccountService;
import com.csc.integralpos.module.services.contact.ContactService;
import com.csc.integralpos.module.user.UserService;
import com.csc.integralpos.testing.TestingSDK;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

//import com.csc.integralpos.sdk.pdf.OnGenPdfListener;

/**
 * @author vdao5
 */
public class IPOSPlugin extends CordovaPlugin implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_LOCATION_PHONE_STATE_STORAGE = 1;


    private static final String PROCESS_ACTION = "executeProcess";

    private static final String ACTION = "action";
    private static final String MESSAGES = "messages";
    private static final String DOCTYPE = "docType";
    private static final String DOCID = "docId";
    private static final String DATA_JSON = "dataJson";
    private static final String PAGE = "page";
    private static final String SIZE = "size";
    private static final String SORT_FIELD = "sort_field";
    private static final String SORT_TYPE = "sort_type";
    private static final String PROJECTION = "projection";

    private static final String ACTION_LOGIN = "login.checklogin";
    private static final String ACTION_GET_PROFILE = "login.getListProfile";
    private static final String ACTION_CHOOSE_PROFILE = "loginAction.choose-profile";
    private static final String ACTION_CHECK_IS_LOGIN = "user.check.isLogin";
    private static final String ACTION_GET_USER_INFO = "login.getUserInfo";
    private static final String ACTION_SAVE_USER_INFO_INTO_LOCAL_DATABASE = "SAVE_USER_INFO_INTO_LOCAL_DATABASE";
    private static final String ACTION_SAVE_ACCESS_TOKEN_TO_USER_DEFAULTS = "SAVE_ACCESS_TOKEN_TO_USER_DEFAULTS";


    private static final String ACTION_DOCTYPE_GET_LAZY_LIST = "doctype.getlist";
    private static final String ACTION_DOCTYPE_GET_SEARCH_V4 = "SEARCH_V4";
    private static final String ACTION_DOCTYPE_SAVE = "doctype.save";
    private static final String ACTION_DOCUMENT_GET_LIST = "get.getList";//"document.getList";
    private static final String ACTION_DOCUMENT_REFRESH = "document.refresh";
    private static final String ACTION_DOCUMENT_COMPUTE = "document.compute";
    private static final String ACTION_DOCUMENT_VALIDATE = "document.validate";
    private static final String ACTION_DOCUMENT_UPDATE = "document.update";
    private static final String ACTION_DOCUMENT_CLONE = "";
    private static final String ACTION_DOCUMENT_CALL_BY_URL = "document.callByURL";
    private static final String ACTION_DOCUMENT_GET_DETAILS = "document.getDocumentDetail";
    private static final String ACTION_DOCUMENT_GET_MODEL = "document.getModel";
    private static final String ACTION_DOCUMENT_PRINT_PDF_V4 = "PRINTPDF_V4";
    private static final String ACTION_CALL_API_ONEMAP = "callAPIOneMap";
    private static final String ACTION_OPERATE_DOCUMENT = "OPERATE_DOCUMENT_BY_DETAIL";
    private static final String ACTION_INIT_DOCUMENT = "INIT_DOCUMENT";
    private static final String ACTION_VALIDATE = "VALIDATE";
    private static final String ACTION_REFRESH = "REFRESH";
    private static final String ACTION_DOCUMENT_LAZY_LIST = "DOCUMENT_LAZY_CHOICELIST";
    private static final String ACTION_CREATE_DOCUMENT = "CREATE_DOCUMENT";
    private static final String ACTION_SEARCH_DOCUMENT = "SEARCH_DOCUMENT";
    private static final String ACTION_DOCUMENT_BY_ID = "OPERATE_DOCUMENT_BY_ID";
    private static final String ACTION_GET_DOCUMENT_TOTAL_RECORDS="GET_DOCUMENT_TOTAL_RECORDS";

    private static final String CONTACT_DOC = "contacts";
    private static final String FNA_DOC = "fnas";
    private static final String FNA_MODULE_SERVICE_NAME = "needs";


    private IposModuleBuilder mIposModuleBuilder;
    private IIposModuleService mIposModuleService;


    // Used when instantiated via reflection by PluginManager
    public IPOSPlugin() {
        super();
        buildProductModules();
    }

    // These can be used by embedders to allow Java-configuration of IPOSPlugin.
    public IPOSPlugin(Context context) {
        super();

    }


    private void buildProductModules() {
        mIposModuleBuilder = new IposModuleBuilder();
        try {
            mIposModuleBuilder.buildModule(IposModuleBuilder.UserType.AGENT_SALE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (VpmsLoadFailedException e) {
            e.printStackTrace();
        }
//        for (IposModuleBuilder.Module module : IposModuleBuilder.Module.values()) {
//            try {
//                mIposModuleBuilder.buildModule(module);
//            } catch (Exception e) {
//                e.printStackTrace();
//                continue;
//            }
//        }
    }


    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this.cordova.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this.cordova.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this.cordova.getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
    }

    public void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this.cordova.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this.cordova.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this.cordova.getActivity(), Manifest.permission.READ_PHONE_STATE)) {

            Toast.makeText(this.cordova.getActivity(), this.cordova.getActivity().getString(R.string.request_permission), Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(this.cordova.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE},
                    REQUEST_LOCATION_PHONE_STATE_STORAGE);
        } else {
            ActivityCompat.requestPermissions(this.cordova.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE},
                    REQUEST_LOCATION_PHONE_STATE_STORAGE);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (PROCESS_ACTION.equals(action)) {
            checkPermission();
            //always get here
            final JSONObject requestObject = args.getJSONObject(0);
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String requestAction = requestObject.getString(ACTION);
                        JSONObject requestMessages = requestObject.getJSONObject(MESSAGES);
                        String doctype = "";
                        String action = "";

//                        TestingSDK testing = new TestingSDK();
//                        testing.testShowPDF(IPOSPlugin.this.cordova.getActivity(), "");
//                        testing.testPersonalContact(ApplicationConfig.getContext());
//                        testing.testDocumentModel(ApplicationConfig.getContext());
//                        testing.testDateTime();
//                        testing.testLogin(ApplicationConfig.getContext());
//                        testing.testProfile(ApplicationConfig.getContext());
//                        testing.testPersonalContact(ApplicationConfig.getContext());
//                        testing.testPaymentModel(ApplicationConfig.getContext());
//                        testing.testApplication(ApplicationConfig.getContext());
//                        testing.testCase(ApplicationConfig.getContext());
//                        testing.testFNA(ApplicationConfig.getContext());
                        //Get Module Service based on doctype (except login, it doesn't have doctype)
                        try {
                            doctype = requestMessages.getString(DOCTYPE);
                            String moduleServiceName = doctype;
                            if(doctype.equalsIgnoreCase(FNA_DOC)) moduleServiceName = FNA_MODULE_SERVICE_NAME;
                            mIposModuleService = mIposModuleBuilder.getModuleService(moduleServiceName);
                            action = requestMessages.getString(ACTION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(ACTION_OPERATE_DOCUMENT.equals(requestAction)){
                           if(ACTION_REFRESH.equals(action) || action.equals("")){
                                JSONObject data = requestMessages.optJSONObject(DATA_JSON);
                                mIposModuleService.initObjectFromJson(data.toString());
                                JSONObject response = mIposModuleService.refreshResponse();
//                                TestingSDK sdk = new TestingSDK();
//                                String dataContact = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"init-contact.json");
//                                JSONObject response = new JSONObject(dataContact);
//                               TestingSDK sdk_ = new TestingSDK();
//                               String initContact = sdk_.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"init-contact-new-ui.json");
//                               JSONObject response = new JSONObject(initContact);
                                callbackContext.success(response);
                            } else if(ACTION_VALIDATE.equals(action)){
                                JSONObject data = requestMessages.optJSONObject(DATA_JSON);
                                mIposModuleService.initObjectFromJson(data.toString());
                                JSONObject response = mIposModuleService.refreshResponse();
                                callbackContext.success(response);
                            }
                            else{
                                  TestingSDK sdk = new TestingSDK();
                                  String dataContact = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"init-contact.json");
                                  JSONObject response = new JSONObject(dataContact);
                                  callbackContext.success(response);
                           }
                        } else if(ACTION_INIT_DOCUMENT.equals(requestAction)) {
                            /*TestingSDK sdk = new TestingSDK();
                            String initContact = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"init-contact.json");
                            JSONObject response = new JSONObject(initContact);*/
                            JSONObject response = null;
                            switch (doctype) {
                                case CONTACT_DOC:
//                                    TestingSDK sdk_ = new TestingSDK();
//                                    String initContact = sdk_.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"init-contact-new-ui.json");
//                                    response = new JSONObject(initContact);
                                    response = ((ContactService)mIposModuleService).initialResponse();
                                    break;
                                case FNA_DOC:
                                    // response = ((FNAService)mIposModuleService).initialResponse();
                                    TestingSDK sdk = new TestingSDK();
                                    String initFNA = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"init-fna.json");
                                    response = new JSONObject(initFNA);
                                    break;
                            }
                            // ((ContactService)mIposModuleService)
                            callbackContext.success(response);
                        } else if(ACTION_DOCUMENT_LAZY_LIST.equals(requestAction)){
//                            String lstFields = requestMessages.getString("listDropdown");
//                            JSONObject response = new LazyListUtils().getLazyRestrictionList(lstFields);
                            TestingSDK sdk = new TestingSDK();
                            String list = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"lazy-list.json");
                            JSONObject response = new JSONObject(list);
                            callbackContext.success(response);
                        } else if(ACTION_CREATE_DOCUMENT.equals(requestAction)){
                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
                            //update username
                            String ownerName=((ContactService)mIposModuleService).getUser();
                            data.optJSONObject("metaData").put("ownerName",ownerName);
                            mIposModuleService.initObjectFromJson(data.toString());

//                            JSONObject response = mIposModuleService.initialResponse();
                            JSONObject response = ((ContactService)mIposModuleService).insertOrUpdateDbNew();
                            callbackContext.success(response);
                        } else if(ACTION_SEARCH_DOCUMENT.equals(requestAction)){
                            //JSONObject response = mIposModuleService.getListItems(Constants.MODIFY_DATE, Constants.DESC);//new JSONObject(metas);//
                            Integer page=Integer.parseInt(requestMessages.optString("page"));
                            mIposModuleService=mIposModuleBuilder.getModuleService(ServiceConstants.USER_SERVICE);
                            String ownerName=((UserService)mIposModuleService).getUser();
                            String newLogin=((UserService)mIposModuleService).getNewLogin();
                            mIposModuleService=mIposModuleBuilder.getModuleService(ServiceConstants.CONTACT_SERVICE);
                            JSONObject response = ((ContactService)mIposModuleService).getListItemsNew(newLogin,ownerName,page);//new JSONObject(metas);//
                             callbackContext.success(response);
//                            TestingSDK sdk = new TestingSDK();
//                            String list = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"get-document-total.json");
//                            JSONObject response = new JSONObject(list);
//                              callbackContext.success(response);
                        } else if(ACTION_DOCUMENT_BY_ID.equals(requestAction)){
                            JSONObject data;
                            String uuid="";
                            try {
                                data = requestMessages.optJSONObject(DATA_JSON);
                                uuid=data.optString("_id");
                            } catch (Exception e) {
                                data=null;
                            }
                            if(action.equals("update") || data!=null){
                                ((ContactService)mIposModuleService).upDateNew(data,uuid, Constants.ACTION_TYPE_UPDATE);
                                callbackContext.success(data);
                            } else {
                                uuid = requestMessages.getString(DOCID);
                                JSONObject response = ((ContactService)mIposModuleService).getItemNew(uuid);
//                                TestingSDK sdk = new TestingSDK();
//                                String list = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"get-document-by-id.json");
//                                JSONObject response = new JSONObject(list);
                                callbackContext.success(response);
                            }
                        }else if(ACTION_GET_DOCUMENT_TOTAL_RECORDS.equals(requestAction)){
//                               mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.USER_SERVICE);
//                               String ownerName=((UserService)mIposModuleService).getUser();
//                               mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.SYNC_SERVICE);
//                               ((SyncService)mIposModuleService).syncContact("contacts",ownerName);
                            TestingSDK sdk = new TestingSDK();
                            String list = sdk.loadJSONFromAsset(IPOSPlugin.this.cordova.getActivity(),"get-document-total.json");
                            JSONObject response = new JSONObject(list);
                            callbackContext.success(response);
                        }
                        else if(ACTION_SAVE_USER_INFO_INTO_LOCAL_DATABASE.equals(requestAction))
                        {
//                             mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.CONTACT_SERVICE);
//                             String ownerName=((ContactService)mIposModuleService).getUser();
//                            ((ContactService)mIposModuleService).syncContact("contacts",ownerName);
                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
                            mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.ACCOUNT_SERVICE);
                            JSONObject response= ((AccountService)mIposModuleService).insertOrUpdateDbNew(data);
                            //((AccountService)mIposModuleService).saveUser(data.optString("username"));
                            mIposModuleService=mIposModuleBuilder.getModuleService(ServiceConstants.USER_SERVICE);
                            ((UserService)mIposModuleService).saveUser(data.optString("username"));
                            callbackContext.success(response);
                        }
                        else if(ACTION_SAVE_ACCESS_TOKEN_TO_USER_DEFAULTS.equals(requestAction))
                        {
                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
                            mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.USER_SERVICE);
                            ((UserService)mIposModuleService).saveTokenUser(data);
                            callbackContext.success(data);
                        }
//                        if (ACTION_GET_PROFILE.equals(requestAction)) {
//                            //re-assign mIposModuleService because login request doesn't have doctype
//                            mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.LOGIN_SERVICE);
//                            String userName = requestMessages.getString("userName");
//                            String password = requestMessages.getString("password");
//                            JSONObject response = ((LoginService) mIposModuleService).getAccountProfiles(userName, password);
//                            callbackContext.success(response);
//                        } else if (ACTION_LOGIN.equals(requestAction)) {
//                            //re-assign mIposModuleService because login request doesn't have doctype
//                            mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.LOGIN_SERVICE);
//                            String username = requestMessages.getString("userName");
//                            String password = requestMessages.getString("password");
//                            String profileID = requestMessages.getString("profileID");
//                            String userRole = requestMessages.getString("userRole");
//                            JSONObject response = ((LoginService) mIposModuleService).getTicketStatus(username, password, profileID, userRole);
//                            if (response.has(Constants.ERROR_CODE) && response.has(Constants.ERROR_MSG)) {
//                                callbackContext.error(response);
//                            } else {
//                                callbackContext.success(response);
//                            }
//                        } else if (ACTION_GET_USER_INFO.equalsIgnoreCase(requestAction)) {
//                            mIposModuleService = mIposModuleBuilder.getModuleService(ServiceConstants.LOGIN_SERVICE);
//                            String username = requestMessages.getString("userName");
//                            JSONObject response = ((LoginService) mIposModuleService).getUserInfo(username);
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCTYPE_GET_LAZY_LIST.equalsIgnoreCase(requestAction)) {
//                            String lstFields = requestMessages.getString("listDropdown");
//                            JSONObject response = new LazyListUtils().getLazyRestrictionList(lstFields);
//                            callbackContext.success(response);
//                        } else if (ACTION_CHOOSE_PROFILE.equals(requestAction)) {
//
//                        } else if (ACTION_CHECK_IS_LOGIN.equals(requestAction)) {
//
//                        } else if (ACTION_DOCTYPE_SAVE.equals(requestAction)) {
//                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
//                            mIposModuleService.initObjectFromJson(data.toString());
//                            JSONObject response = mIposModuleService.initialResponse();
//                            mIposModuleService.insertOrUpdateDb();
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_GET_LIST.equals(requestAction)) {
//                            JSONArray arrResponse = ((ContactService) mIposModuleService).getProjectionMetadataList();
//                            callbackContext.success(arrResponse);
//                        } else if (ACTION_DOCTYPE_GET_SEARCH_V4.equalsIgnoreCase(requestAction)) {
//                            String sortField = requestMessages.has(SORT_FIELD) ? requestMessages.getString(SORT_FIELD) : Constants.EMPTY_STRING;
//                            String sortType = requestMessages.has(SORT_TYPE) ? requestMessages.getString(SORT_TYPE) : Constants.EMPTY_STRING;
//                            JSONObject response = mIposModuleService.getListItems(sortField, sortType);
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_REFRESH.equals(requestAction)) {
//                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
//                            mIposModuleService.initObjectFromJson(data.toString());
//                            JSONObject response = mIposModuleService.refreshResponse();
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_VALIDATE.equals(requestAction)) {
//                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
//                            mIposModuleService.initObjectFromJson(data.toString());
//
//                            JSONObject response = mIposModuleService.validateResponse();
//                            callbackContext.success(response);
//
//                        } else if (ACTION_DOCUMENT_COMPUTE.equals(requestAction)) {
//                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
//                            mIposModuleService.initObjectFromJson(data.toString());
//                            JSONObject response = mIposModuleService.computeResponse();
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_UPDATE.equals(requestAction)) {
//                            JSONObject data = requestMessages.optJSONObject(DATA_JSON);
//                            mIposModuleService.initObjectFromJson(data.toString());
//                            mIposModuleService.insertOrUpdateDb();
//                            callbackContext.success(data);
//                        } else if(ACTION_DOCUMENT_CLONE.equals(requestAction)) {
//                            String caseId = requestMessages.getString("caseId");
//                            String quotationId = requestMessages.getString("quotationId");
//
//                        }else if (ACTION_DOCUMENT_CALL_BY_URL.equals(requestAction)) {
//                            String[] value = requestMessages.getString("url").split("/");
//                            String uuid = value[value.length - 1];
//                            JSONObject response = mIposModuleService.getItem(uuid);
//                            callbackContext.success(response);
//                        } else if (ACTION_CALL_API_ONEMAP.equals(requestAction)) {
//                            String postcode = requestMessages.getString("postalCode");
//                            JSONObject response = OneMapAPIUtils.getAddress(postcode);
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_GET_DETAILS.equalsIgnoreCase(requestAction)) {
//                            String docId = requestMessages.getString("docId");
//                            JSONObject response = mIposModuleService.getItem(docId);
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_GET_MODEL.equalsIgnoreCase(requestAction)) {
//                            JSONObject response = new JSONObject(contact);;//mIposModuleService.objectToJson();
//                            callbackContext.success(response);
//                        } else if (ACTION_DOCUMENT_PRINT_PDF_V4.equalsIgnoreCase(requestAction)) {
//                            JSONObject dataJson = requestMessages.getJSONObject("dataJson");
//                            String template = requestMessages.getString("templateName");
//                            String path = ServiceUtils.getTemplatePath(template);
//                            File directory = com.csc.integralpos.sdk.utils.FileUtils.getCacheDirectory(ApplicationConfig.getContext());
//                            File pdfFile = new File(directory, dataJson.getString("uuid") + Constants.PDF_EXTENSION);
//                            PdfHelper pdfHelper = new PdfHelper();
//                            pdfHelper.genPdfWithPath(IPOSPlugin.this.cordova.getActivity(),
//                                    pdfFile.getAbsolutePath(), path, dataJson.toString(),
//                                    new OnGenPdfListener() {
//                                        @Override
//                                        public void startGeneratePdf() {
//                                            Log.d("genPdfWithCache", "startGeneratePdf");
//                                        }
//
//                                        @Override
//                                        public void finishGenerateSuccessful(File file) {
//                                            try {
//                                                byte[] bytes = FileUtils.readFileToByteArray(file);
//                                                String content = Base64.encodeToString(bytes, Base64.DEFAULT);
//                                                JSONObject response = new JSONObject();
//                                                response.put("fileData", content);
//                                                callbackContext.success(response);
//                                                Log.d("genPdfWithCache", "finishGenerateSuccessful");
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//
//                                        @Override
//                                        public void finishGenerateFailed(File file) {
//                                            JSONObject result = new JSONObject();
//                                            try {
//                                                result.put("errCode", "1000");
//                                            } catch (JSONException e1) {
//                                                e1.printStackTrace();
//                                            }
//                                            callbackContext.success(result);
//                                            Log.d("genPdfWithCache", "finishGenerateFailed");
//                                        }
//                                    });
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject result = new JSONObject();
                        try {
                            result.put("errCode", "1000");
                            result.put("errMsg", e.getMessage());
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        callbackContext.error(result);
                    }
                }

            });

            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PHONE_STATE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this.cordova.getActivity(), this.cordova.getActivity().getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
