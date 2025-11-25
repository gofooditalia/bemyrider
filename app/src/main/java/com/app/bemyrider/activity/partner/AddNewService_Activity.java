package com.app.bemyrider.activity.partner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.app.bemyrider.Adapter.Partner.GridViewAdapter;
import com.app.bemyrider.AsyncTask.DownloadAsync;
import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.databinding.PartnerActivityAddNewServiceBinding;
import com.app.bemyrider.helper.PermissionUtils;
import com.app.bemyrider.helper.ToastMaster;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.FileUtilPOJO;
import com.app.bemyrider.model.ProviderServiceDetailsItem;
import com.app.bemyrider.model.ProviderServiceMediaDataItem;
import com.app.bemyrider.model.ServiceDataItem;
import com.app.bemyrider.model.ServiceListPOJO;
import com.app.bemyrider.model.partner.EditProfilePojo;
import com.app.bemyrider.model.partner.ImageItem;
import com.app.bemyrider.model.partner.SubCategoryItem;
import com.app.bemyrider.model.partner.SubCategoryListPojo;
import com.app.bemyrider.model.user.CategoryDataItem;
import com.app.bemyrider.model.user.CategoryListPOJO;
import com.app.bemyrider.utils.ConnectionManager;
import com.app.bemyrider.utils.FileUtils;
import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import com.app.bemyrider.utils.PrefsUtil;
import com.app.bemyrider.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Modified by Hardik Talaviya on 6/12/19.
 */


public class AddNewService_Activity extends AppCompatActivity implements GridViewAdapter.Image_interface {

    private static final String TAG = "AddNewServiceActivity";
    private Context mContext;
    private Activity mActivity;
    private PermissionUtils permissionUtils;

    private static final int SELECT_PHOTO = 100;
    private PartnerActivityAddNewServiceBinding binding;
    //To add new service
    private ArrayList<ImageItem> imageItems;
    private ProviderServiceDetailsItem editServiceData;
    private String strselectedCategoryId, strselectedSubCategoryId, strselectedServiceId,
            selectedHours = "", imageEncoded;
    private GridViewAdapter gridAdapter;
    private ConnectionManager connectionManager;

    private ArrayAdapter categoryAdapter, subcategoryAdapter, serviceAdapter;

    private ArrayList<CategoryDataItem> categoryListItems = new ArrayList<>();
    private ArrayList<SubCategoryItem> subcategoryListItems = new ArrayList<>();
    private ArrayList<ServiceDataItem> serviceListItems = new ArrayList<>();
    private ArrayList<ProviderServiceMediaDataItem> mediadata = new ArrayList<>();
    private ArrayList<File> filearrayList = new ArrayList<>();

    private ArrayList<String> imagesEncodedList;

    private ArrayAdapter hoursAdapter;
    private String[] hours_array = {"Select Hours To Complete*", "1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "6 Hours",
            "7 Hours", "8 Hours", "9 Hours", "10 Hours", "11 Hours", "12 Hours", "13 Hours", "14 Hours", "15 Hours", "16 Hours", "17 Hours",
            "18 Hours", "19 Hours", "20 Hours", "21 Hours", "22 Hours", "23 Hours", "24 Hours"};

    private AsyncTask addServiceAsync, serviceListAsync, subCatListAsync, catListAsync,
            deleteMediaAsync;

    private ActivityResultLauncher<Uri> actResCamera;
    private ActivityResultLauncher<Intent> actResGallery;

    private String selectedImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        binding = DataBindingUtil.setContentView(this, R.layout.partner_activity_add_new_service);
        mContext = binding.getRoot().getContext();
        mActivity = AddNewService_Activity.this;

        if (getIntent().hasExtra("data")) {
            if (getIntent().getSerializableExtra("data") != null) {
                editServiceData = (ProviderServiceDetailsItem) getIntent().getSerializableExtra("data");
            }
        }

        permissionUtils = new PermissionUtils(mActivity, mContext, new PermissionUtils.OnPermissionGrantedListener() {
            @Override
            public void onCameraPermissionGranted() {
                selectedImagePath = Utils.openCamera(mContext, actResCamera);
            }

            @Override
            public void onStoragePermissionGranted() {
                selectedImagePath = "";
                Utils.openImagesDocument(actResGallery);
            }
        });

        initView();
        fillData();
        serviceCategoryCall();

        binding.spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strselectedServiceId = serviceListItems.get(position).getServiceId();
                if (position != 0) {
                    if (serviceListItems.get(position).getServiceType().equals("hourly")) {
                        binding.spinnerSelectProposalHours.setVisibility(View.GONE);
                        //binding.viewSpHour.setVisibility(View.GONE);
//                        binding.txtPerHours.setText(R.string.per_hours);
                    } else {
                        binding.spinnerSelectProposalHours.setVisibility(View.VISIBLE);
                        //binding.viewSpHour.setVisibility(View.VISIBLE);
//                        binding.txtPerHours.setText(R.string.fixed);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerCatergory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strselectedCategoryId = categoryListItems.get(position).getCategoryId();
                if (categoryListItems.get(position).getCategoryId().equals("0")) {
                    subcategoryListItems.clear();
                    subcategoryListItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
                    subcategoryAdapter.notifyDataSetChanged();
                } else {
                    serviceCallSubCategory(strselectedCategoryId);
                }
                Log.e("Selected Cat", strselectedCategoryId);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerSubcatergory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strselectedSubCategoryId = subcategoryListItems.get(position).getCategoryId();
                if (subcategoryListItems.get(position).getCategoryId().equals("0")) {
                    serviceListItems.clear();
                    serviceListItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
                    serviceAdapter.notifyDataSetChanged();
                } else {
                    serviceCallServiceList(strselectedSubCategoryId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerSelectProposalHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.spinnerSelectProposalHours.getSelectedItemPosition() != 0) {
                    String hour[] = hours_array[position].split(" ");
                    selectedHours = hour[0];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.etAddPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillAddPrices.setError("");
                binding.tillAddPrices.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.etAddDiscription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tillAddDiscription.setError("");
                binding.tillAddDiscription.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.TxtUpload.setOnClickListener(view -> {
            openCameraGalleryDialog();
        });

        binding.btnaddservice.setOnClickListener(v -> {
            if (checkValidation()) {
                binding.btnaddservice.setClickable(false);
                serviceCall();
            }
        });
    }

    private void fillData() {
        if (editServiceData != null) {
            binding.etAddPrice.setText(editServiceData.getPrice());
            binding.etAddDiscription.setText(Utils.decodeEmoji(editServiceData.getDescription()));

            for (int i = 0; i < editServiceData.getMediaData().size(); i++) {
                filearrayList.add(new File(editServiceData.getMediaData().get(i).getMediaUrl()));
                String name = editServiceData.getMediaData().get(i).getMediaUrl()
                        .substring(editServiceData.getMediaData().get(i).getMediaUrl()
                                .lastIndexOf("/") + 1);
                imageItems.add(new ImageItem(Uri.parse(editServiceData.getMediaData()
                        .get(i).getMediaUrl()), new File(editServiceData.getMediaData().get(i).getMediaUrl()), name));
            }
            gridAdapter = new GridViewAdapter(mContext, R.layout.partner_gridlayout_itermrow, editServiceData.getMediaData(), imageItems,
                    this);
            binding.gridView.setAdapter(gridAdapter);

            try {
                if (!TextUtils.isEmpty(editServiceData.getHours())) {
                    int pos = Integer.parseInt(editServiceData.getHours());
                    if (pos < hours_array.length) {
                        binding.spinnerSelectProposalHours.setSelection(pos);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*------------- Validation ---------------*/
    private boolean checkValidation() {

        if (!(binding.spinnerCatergory.getSelectedItemPosition() > 0)) {
            Toast.makeText(mContext, R.string.please_select_category, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!(binding.spinnerSubcatergory.getSelectedItemPosition() > 0)) {
            Toast.makeText(mContext, R.string.please_select_subcategory, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!(binding.spinnerService.getSelectedItemPosition() > 0)) {
            Toast.makeText(mContext, R.string.please_select_services, Toast.LENGTH_SHORT).show();
            return false;
        }
        /*if (binding.spinnerSelectProposalHours.getSelectedItemPosition() == 0) {
            if (binding.spinnerSelectProposalHours.getVisibility() == View.VISIBLE) {
                Toast.makeText(context, R.string.hours_to_complete, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        }*/
        String strPricePerHour = binding.etAddPrice.getText().toString().trim();
        if (strPricePerHour.isEmpty()) {
            binding.tillAddPrices.setErrorEnabled(true);
            binding.tillAddPrices.setError(getResources().getString(R.string.error_required));
            return false;
        }

        float pricePerHour = Float.parseFloat(strPricePerHour);
        if (pricePerHour > 12.5f) {
            Toast.makeText(mContext, R.string.err_msg_price_per_hour, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etAddPrice.getText().toString().trim().isEmpty()) {
            binding.tillAddPrices.setErrorEnabled(true);
            binding.tillAddPrices.setError(getResources().getString(R.string.error_required));
            return false;
        }
        if (binding.etAddDiscription.getText().toString().trim().isEmpty()) {
            binding.tillAddDiscription.setErrorEnabled(true);
            binding.tillAddDiscription.setError(getResources().getString(R.string.error_required));
            return false;
        }

        return true;
    }

    /*------------------- Add/Edit Service Api Call ---------------------*/
    private void serviceCall() {
        binding.pgSubmit.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
        LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();

        if (editServiceData != null) {
            textParams.put("provider_service_id", editServiceData.getId());
        }
        textParams.put("user_id", PrefsUtil.with(mContext).readString("UserId"));
        textParams.put("service_id", strselectedServiceId);
        textParams.put("category_id", strselectedCategoryId);
        textParams.put("subcategory_id", strselectedSubCategoryId);
        textParams.put("price", binding.etAddPrice.getText().toString().trim());
        textParams.put("description", Utils.encodeEmoji(binding.etAddDiscription.getText().toString().trim()));
        if (binding.spinnerSelectProposalHours.getVisibility() == View.VISIBLE) {
            textParams.put("hours", selectedHours);
        }
        for (int i = 0; i < imageItems.size(); i++) {
            for (int j = 0; j < filearrayList.size(); j++) {
                if (imageItems.get(i).getImageFile().equals(filearrayList.get(j))) {
                    imageItems.remove(i);
                }
            }
        }
        if (imageItems.size() > 0) {
            for (int i = 0; i < imageItems.size(); i++) {
                fileParams.put("service_image[" + i + "]", imageItems.get(i).getImageFile());
            }
        }

        new WebServiceCall(mContext, WebServiceUrl.URL_ADD_NEW_SERVICES, textParams, fileParams,
                EditProfilePojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSubmit.setVisibility(View.GONE);
                binding.btnaddservice.setClickable(true);
                if (status) {
                    deleteFiles();
                    Intent i = new Intent(mContext, Partner_MyServices_Activity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                addServiceAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                addServiceAsync = null;
            }
        });
    }

    /*-------------- Service Api Call -------------------*/
    private void serviceCallServiceList(String strselectedSubCategoryId) {
        binding.spinnerService.setVisibility(View.GONE);
        binding.pgService.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        if (editServiceData != null) {
            textParams.put("provider_service_id", editServiceData.getId());
        }
        textParams.put("user_id", PrefsUtil.with(AddNewService_Activity.this).readString("UserId"));
        textParams.put("subcategory_id", strselectedSubCategoryId);

        new WebServiceCall(mContext, WebServiceUrl.URL_SERVICELIST, textParams, ServiceListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgService.setVisibility(View.GONE);
                binding.spinnerService.setVisibility(View.VISIBLE);
                if (status) {
                    ServiceListPOJO item = (ServiceListPOJO) obj;
                    serviceListItems.clear();
                    serviceListItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
                    serviceListItems.addAll(item.getData());
                    serviceAdapter.notifyDataSetChanged();
                    binding.spinnerService.setSelection(0);

                    if (editServiceData != null) {
                        for (int i = 0; i < serviceListItems.size(); i++) {
                            if (serviceListItems.get(i).getServiceId().equalsIgnoreCase(editServiceData.getServiceId())) {
                                binding.spinnerService.setSelection(i);
                            }
                        }
                    }

                } else {
                    serviceListItems.clear();
                    serviceListItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
                    binding.spinnerService.setSelection(0);
                    Toast.makeText(AddNewService_Activity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                serviceListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                serviceListAsync = null;
            }
        });

    }

    /*------------------ Sub Category Api Call -----------------------*/
    private void serviceCallSubCategory(String strselectedCategoryId) {
        binding.spinnerSubcatergory.setVisibility(View.GONE);
        binding.pgSubCat.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("category_id", strselectedCategoryId);

        new WebServiceCall(mContext, WebServiceUrl.URL_SUBCATEGORYLIST, textParams, SubCategoryListPojo.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgSubCat.setVisibility(View.GONE);
                binding.spinnerSubcatergory.setVisibility(View.VISIBLE);
                if (status) {
                    SubCategoryListPojo item = (SubCategoryListPojo) obj;
                    subcategoryListItems.clear();
                    subcategoryListItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
                    subcategoryListItems.addAll(item.getData());
                    subcategoryAdapter.notifyDataSetChanged();
                    binding.spinnerSubcatergory.setSelection(0);

                    if (editServiceData != null) {
                        for (int i = 0; i < subcategoryListItems.size(); i++) {

                            if (subcategoryListItems.get(i).getCategoryId().equalsIgnoreCase(editServiceData.getSubcategoryId())) {
                                binding.spinnerSubcatergory.setSelection(i);
                            }
                        }
                    }

                } else {
                    subcategoryListItems.clear();
                    subcategoryListItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
                    binding.spinnerSubcatergory.setSelection(0);
                    Toast.makeText(AddNewService_Activity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                subCatListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                subCatListAsync = null;
            }
        });
    }

    /* ----------------- Category Api Call --------------------- */
    private void serviceCategoryCall() {
        binding.spinnerCatergory.setVisibility(View.GONE);
        binding.pgCat.setVisibility(View.VISIBLE);

        new WebServiceCall(mContext, WebServiceUrl.URL_CATEGORYLIST, new LinkedHashMap<>(),
                CategoryListPOJO.class, false, new WebServiceCall.OnResultListener() {
            @Override
            public void onResult(boolean status, Object obj) {
                binding.pgCat.setVisibility(View.GONE);
                binding.spinnerCatergory.setVisibility(View.VISIBLE);
                if (status) {
                    CategoryListPOJO item = (CategoryListPOJO) obj;
                    categoryListItems.addAll(item.getData());
                    categoryAdapter.notifyDataSetChanged();

                    if (editServiceData != null) {
                        for (int i = 0; i < categoryListItems.size(); i++) {
                            if (categoryListItems.get(i).getCategoryId()
                                    .equalsIgnoreCase(editServiceData.getCategoryId())) {
                                binding.spinnerCatergory.setSelection(i);
                            }
                        }
                    }
                } else {
                    Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAsync(AsyncTask asyncTask) {
                catListAsync = asyncTask;
            }

            @Override
            public void onCancelled() {
                catListAsync = null;
            }
        });

    }

    /*------------------ Delete Image Api Call ----------------------*/
    private void serviceCallDeleteImage(String mediaId, final int position, ProgressBar pgDelete, ImageView imgDelete) {
        imgDelete.setVisibility(View.GONE);
        pgDelete.setVisibility(View.VISIBLE);

        LinkedHashMap<String, String> textParams = new LinkedHashMap<>();

        textParams.put("media_id", mediaId);

        textParams.put("user_id", PrefsUtil.with(AddNewService_Activity.this).readString("UserId"));


        new WebServiceCall(AddNewService_Activity.this,
                WebServiceUrl.URL_DELETE_SERVICE_IMAGE, textParams, CommonPojo.class, false,
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        pgDelete.setVisibility(View.GONE);
                        imgDelete.setVisibility(View.VISIBLE);
                        imgDelete.setClickable(true);
                        if (status) {
                            try {
                                gridAdapter.removeData(position);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            filearrayList.remove(position);
                            gridAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(mContext, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAsync(AsyncTask asyncTask) {
                        deleteMediaAsync = asyncTask;
                    }

                    @Override
                    public void onCancelled() {
                        deleteMediaAsync = null;
                    }
                });

    }

    private void openCameraGalleryDialog() {
        final Dialog d = new Dialog(mActivity);
        d.setContentView(getLayoutInflater().inflate(R.layout.dialog_camera_gallery, null));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = d.getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        LinearLayoutCompat linCamera = d.findViewById(R.id.linCamera);
        LinearLayoutCompat linGallery = d.findViewById(R.id.linGallery);

        linCamera.setOnClickListener(view -> {
            d.dismiss();
            selectedImagePath = "";
            permissionUtils.checkCameraPermission();
        });

        linGallery.setOnClickListener(view -> {
            d.dismiss();
            permissionUtils.checkStoragePermission();
        });
        d.show();
    }

    @Override
    public void deleteimage(String mediaId, int id, ProgressBar pgDelete, ImageView imgRemove) {
        try {
            imgRemove.setClickable(false);
            serviceCallDeleteImage(mediaId, id, pgDelete, imgRemove);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void addPhotoToList(String path) {
        String name = path.substring(path.lastIndexOf("/") + 1);
        imageItems.add(new ImageItem(Uri.fromFile(new File(path)), new File(path), name));
        try {
            gridAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public String compressImage(String imageUri) {
        String filePath = imageUri;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//		by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//		you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//		max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//		width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

//		setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//		inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//		this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//			load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//		check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            android.util.Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                android.util.Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                android.util.Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                android.util.Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
//			write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), getString(R.string.app_name) + "/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + "JPEG_" + System.currentTimeMillis() + "_" + ".jpg");
        return uriSting;
    }

    public void deleteFiles() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), getString(R.string.app_name) + "/Images");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void permissionMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(getString(R.string.cancelling_granted)).show();
    }

    private void initView() {
        if (editServiceData != null) {
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.edit_service),HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            setTitle(HtmlCompat.fromHtml("<font color=#FFFFFF>" + getString(R.string.add_new_service),HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*Init Internet Connection Class For No Internet Banner*/
        connectionManager = new ConnectionManager(mContext);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(mContext);

        imageItems = new ArrayList<>();

        binding.gridView.setExpanded(true);
        try {
            gridAdapter = new GridViewAdapter(this,
                    R.layout.partner_gridlayout_itermrow, mediadata, imageItems,
                    this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        binding.gridView.setAdapter(gridAdapter);

        /*Init Category Spinner*/
        categoryAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, categoryListItems);
        categoryListItems.add(new CategoryDataItem("0", getString(R.string.select_category)));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCatergory.setAdapter(categoryAdapter);

        /*Init Sub Category Spinner*/
        subcategoryAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, subcategoryListItems);
        subcategoryListItems.add(new SubCategoryItem("0", getString(R.string.select_subcategory)));
        subcategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSubcatergory.setAdapter(subcategoryAdapter);

        /*Init Service Spinner*/
        serviceAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, serviceListItems);
        serviceListItems.add(new ServiceDataItem("0", getString(R.string.select_service)));
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerService.setAdapter(serviceAdapter);

        /*Init Hour Spinner*/
        hoursAdapter = new ArrayAdapter<>(AddNewService_Activity.this, android.R.layout.simple_spinner_item, hours_array);
        binding.spinnerSelectProposalHours.setAdapter(hoursAdapter);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        initActivityResult();
    }

    private void initActivityResult() {
        actResCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                try {
                    Uri imageUri = Uri.parse(selectedImagePath);
                    FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
                    addPhotoToList(compressImage(fileUtils.getPath()));
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult: " + e.getMessage());
                }
            }
        });

        actResGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                                Uri imageUri = data.getData();
                                FileUtilPOJO fileUtils = FileUtils.getPath(mContext, imageUri);
                                if (fileUtils.isRequiredDownload()) {
                                    String[] strArr = fileUtils.getPath().split(",");
                                    new DownloadAsync(AddNewService_Activity.this, Uri.parse(strArr[2]),
                                            strArr[0], strArr[1], downloadResult ->
                                            //addPhotoToList(downloadResult)
                                            addPhotoToList(compressImage(downloadResult))
                                    ).execute();
                                } else {
                                    addPhotoToList(compressImage(fileUtils.getPath()));
                                    //  addPhotoToList(fileUtils.getPath());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: " + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CODE_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_camera);
            } else {
                permissionUtils.checkCameraPermission();
            }
        } else if (requestCode == PermissionUtils.REQ_CODE_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastMaster.showShort(mContext, R.string.err_permission_storage);
            } else {
                permissionUtils.checkStoragePermission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.cancelAsyncTask(addServiceAsync);
        Utils.cancelAsyncTask(serviceListAsync);
        Utils.cancelAsyncTask(subCatListAsync);
        Utils.cancelAsyncTask(catListAsync);
        Utils.cancelAsyncTask(deleteMediaAsync);

        /** clear cache dir of picture which is taken photo from camera */
        Utils.clearCameraCache(mContext);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}


