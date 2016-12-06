package com.cwc.litenote.config;


import java.io.File;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.util.Util;

import de.psdev.licensesdialog.LicensesDialogFragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class Config extends Fragment 
{

	// style
//	SharedPreferences mPref_style;
	TextView mNewPageTVStyle;
	private int mStyle = 0;

	// vibration
	SharedPreferences mPref_vibration;
	SharedPreferences mPref_FinalPageViewed;
	TextView mTextViewVibration;

	private AlertDialog dialog;
	private Context mContext;
	private LayoutInflater mInflator;
//	String[] mItemArray = new String[]{"black","white","greenish","reddish","bluish","yellowish"};
	String[] mItemArray = new String[]{"1","2","3","4","5","6","7","8","9","10"};
	
	public Config(){};
	View mRootView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ Config / onCreateView ==================");
		
		mRootView = inflater.inflate(R.layout.config, container, false);
		
	    //Set text style
		setNewPageTextStyle();
		
		//Set Take Picture Option
		setTakeImageOption();
		
		//Set deleting warning 
		setDeleteWarn();
		
		//Set vibration time length
		setVibratoinTimeLength();
		
		//save all notes to SD card
		exportToSdCardDialog(DB.getFocus_tabsTableId());
		
		//save all notes to SD card
		importFromSdCardDialog();
		
		//set to default
		deleteDB_button();
		
		// show licenses
		showLicensesDlg();
		
		// show About
		showAboutDlg();
		
		// disable button when API version larger or equal than 11
		mRootView.findViewById(R.id.btnConfig).setVisibility(View.GONE); // 1 invisible
		
		return mRootView;
	}   	
  
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		System.out.println("================ Config / onCreate ==================");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		System.out.println("================ Config / onResume ==================");
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		System.out.println("================ Config / onDestroy ==================");
	}
	
	@Override
	public void onDestroyView() 
	{
		super.onDestroyView();
		System.out.println("================ Config / onDestroyView ==================");
	};
	
	
	/**
	 *  set take picture option
	 *  
	 */
	// vibration
	SharedPreferences mPref_takePicture;
	TextView mTextViewTakePicture;	
	void setTakeImageOption()
	{
		//  set current
		mPref_takePicture = getActivity().getSharedPreferences("takeImage", 0);
		View viewVibration = mRootView.findViewById(R.id.takePictureOption);
		mTextViewTakePicture = (TextView)mRootView.findViewById(R.id.TakePictureOptionSetting);
		
		if(mPref_takePicture.getString("KEY_SHOW_CONFIRMATION_DIALOG","no").equalsIgnoreCase("yes"))		   
			mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_yes).toString());
		else
			mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_no).toString());

		// Select new 
		viewVibration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				selectTakePictureOptionDialog();
			}
		});
	}

	void selectTakePictureOptionDialog()
	{
		   final String[] items = new String[]{
				   getResources().getText(R.string.confirm_dialog_button_yes).toString(),
				   getResources().getText(R.string.confirm_dialog_button_no).toString()   };
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   
		   String strTakePicture = mPref_takePicture.getString("KEY_SHOW_CONFIRMATION_DIALOG","no");
		   
		   // add current selection
		   for(int i=0;i< items.length;i++)
		   {
			   if(strTakePicture.equalsIgnoreCase("yes"))
				   items[0] = getResources().getText(R.string.confirm_dialog_button_yes).toString() + " *";
			   else if(strTakePicture.equalsIgnoreCase("no"))
				   items[1] = getResources().getText(R.string.confirm_dialog_button_no).toString() + " *";
		   }
		   
		   DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		   {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0)
					{
						mPref_takePicture.edit().putString("KEY_SHOW_CONFIRMATION_DIALOG","yes").commit();
						mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_yes).toString());
					}
					else if(which == 1)
					{
						mPref_takePicture.edit().putString("KEY_SHOW_CONFIRMATION_DIALOG","no").commit();
						mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_no).toString());
					}
					
					//end
					dialog.dismiss();
				}
		   };
		   builder.setTitle(R.string.config_confirm_taken_picture)
				  .setSingleChoiceItems(items, -1, listener)
				  .setNegativeButton(R.string.btn_Cancel, null)
				  .show();
	}
	
	
	
    /**
     * Save SD Card 
     * 
     */
    void exportToSdCardDialog(final int tabs_tableId)
    {
		final View tvSaveSdCard =  mRootView.findViewById(R.id.exportToSdCard);


		tvSaveSdCard.setOnClickListener(new OnClickListener() 
		{   @Override
			public void onClick(View v) 
			{
				DB dbTabs = new DB(getActivity(),tabs_tableId);
				dbTabs.initTabsDb(dbTabs);
				if(dbTabs.getTabsCount(true)>0)
				{
					Intent intent = new Intent(getActivity(), Export_toSDCardAct.class);
		        	startActivityForResult(intent, EXPORT_TO_SD_CARD);					
				}
				else
				{
					Toast.makeText(getActivity(), R.string.config_export_none_toast, Toast.LENGTH_SHORT).show();
				}
			}
		});
    }
    
    /**
     * Import from SD Card 
     * 
     */
    final int IMPORT_FROM_SD_CARD = 1;
    final int EXPORT_TO_SD_CARD = 2;
    void importFromSdCardDialog()
    {
		View tvImportSdCard =  mRootView.findViewById(R.id.importFmSdCard);

		tvImportSdCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), Import_fromSDCardAct.class);
	        	startActivityForResult(intent, IMPORT_FROM_SD_CARD);
			}
		});
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if( (requestCode == IMPORT_FROM_SD_CARD) ||
			(requestCode == EXPORT_TO_SD_CARD)      )
		{
			// To destroy Config fragment when returning from Import_fromSDCardAct class 
			// or Export_toSDCardAct class  
			getActivity().getSupportFragmentManager().popBackStack();
		}
	}	
	
	/**
	 *  select style
	 *  
	 */
	void setNewPageTextStyle()
	{
		// Get current style
		mNewPageTVStyle = (TextView)mRootView.findViewById(R.id.TextViewStyleSetting);
		View mViewStyle = mRootView.findViewById(R.id.setStyle);
		int iBtnId = Util.getNewPageStyle(getActivity());
		
		// set background color with current style 
		mNewPageTVStyle.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		mNewPageTVStyle.setText(mItemArray[iBtnId]);
		mNewPageTVStyle.setTextColor(Util.mText_ColorArray[iBtnId]);
		
		mViewStyle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectStyleDialog(v);
			}
		});
	}
	
	
	void selectStyleDialog(View view)
	{
		mContext = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		
		builder.setTitle(R.string.config_set_style_title)
			   .setPositiveButton(R.string.btn_OK, listener_ok)
			   .setNegativeButton(R.string.btn_Cancel, null);
		
		// inflate select style layout
		mInflator= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = mInflator.inflate(R.layout.select_style, null);
		RadioGroup RG_view = (RadioGroup)view.findViewById(R.id.radioGroup1);
		
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio0),0);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio1),1);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio2),2);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio3),3);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio4),4);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio5),5);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio6),6);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio7),7);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio8),8);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio9),9);
		
		builder.setView(view);

		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup RG, int id) {
				mStyle = RG.indexOfChild(RG.findViewById(id));
		}});
		
		dialog = builder.create();
		dialog.show();
	}
	
    private void setButtonColor(RadioButton rBtn,int iBtnId)
    {
		rBtn.setBackgroundColor(Util.mBG_ColorArray[iBtnId]);
		rBtn.setText(mItemArray[iBtnId]);
		rBtn.setTextColor(Util.mText_ColorArray[iBtnId]);
		
		//set checked item
		if(iBtnId == Util.getNewPageStyle(mContext))
			rBtn.setChecked(true);
		else
			rBtn.setChecked(false);
    }
		   
    DialogInterface.OnClickListener listener_ok = new DialogInterface.OnClickListener()
   {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			SharedPreferences mPref_style = getActivity().getSharedPreferences("style", 0);
			mPref_style.edit().putInt("KEY_STYLE",mStyle).commit();
			// update the style selection directly
			mNewPageTVStyle.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
			mNewPageTVStyle.setText(mItemArray[mStyle]);
			mNewPageTVStyle.setTextColor(Util.mText_ColorArray[mStyle]);
			//end
			dialog.dismiss();
		}
   };
	
	/**
	 *  set deleting warning 
	 *  
	 */
	void setDeleteWarn()
	{
		View deleteWarn = mRootView.findViewById(R.id.deleteWarn); 
		deleteWarn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectWarnItemDlg selectWarnItemDlg = new SelectWarnItemDlg(v,getActivity());
				selectWarnItemDlg.SelectWarnPref();
			}
		});
	}

	/**
	 *  select vibration time length
	 *  
	 */
	void setVibratoinTimeLength()
	{
		//  set current
		mPref_vibration = getActivity().getSharedPreferences("vibration", 0);
		View viewVibration = mRootView.findViewById(R.id.vibrationSetting);
		mTextViewVibration = (TextView)mRootView.findViewById(R.id.TextViewVibrationSetting);
	    String strVibTime = mPref_vibration.getString("KEY_VIBRATION_TIME","25");
		if(strVibTime.equalsIgnoreCase("00"))
			mTextViewVibration.setText(getResources().getText(R.string.config_status_disabled).toString());
		else
			mTextViewVibration.setText(strVibTime +"ms");

		// Select new 
		viewVibration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				selectVibrationLengthDialog();
			}
		});
	}

	void selectVibrationLengthDialog()
	{
		   final String[] items = new String[]{getResources().getText(R.string.config_status_disabled).toString(),
				   		    				"15ms","25ms","35ms","45ms"};
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   
		   String strVibTime = mPref_vibration.getString("KEY_VIBRATION_TIME","25");
		   
		   if(strVibTime.equalsIgnoreCase("00"))
		   {
			   items[0] = getResources().getText(R.string.config_status_disabled).toString() + " *";
		   }
		   else 
		   {
			   for(int i=1;i< items.length;i++)
			   {
				   if(strVibTime.equalsIgnoreCase((String) items[i].subSequence(0,2)))
					   items[i] += " *";
			   }
		   }
		   
		   DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		   {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String len = null;
					
					if(which ==0)
						len = "00";
					else
						len = (String) items[which].subSequence(0,2);
					mPref_vibration.edit().putString("KEY_VIBRATION_TIME",len).commit();
					// change the length directly
					if(len.equalsIgnoreCase("00"))
						mTextViewVibration.setText(getResources().getText(R.string.config_status_disabled).toString());
					else
						mTextViewVibration.setText(len + "ms");					
					
					//end
					dialog.dismiss();
				}
		   };
		   builder.setTitle(R.string.config_set_vibration_title)
				  .setSingleChoiceItems(items, -1, listener)
				  .setNegativeButton(R.string.btn_Cancel, null)
				  .show();
	}

   
   /**
    * Delete DB
    *  
    */
	public void deleteDB_button(){
		View tvDelDB = mRootView.findViewById(R.id.SetDeleteDB);
		tvDelDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmDeleteDB(v);
			}
		});
	}
	
	private void confirmDeleteDB(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.config_delete_DB_confirm_content)
			   .setPositiveButton(R.string.btn_OK, listener_delete_DB)
			   .setNegativeButton(R.string.btn_Cancel, null)
			   .show();
	}

    DialogInterface.OnClickListener listener_delete_DB = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			DB.deleteDB();
			
			//set last tab Id to 0, otherwise TabId will not start from 0 when deleting all
			TabsHostFragment.setLastExist_tabId(0);
			//reset tab Index to 0 
			//fix: select tab over next import amount => clean all => import => export => error
			TabsHostFragment.mCurrent_tabIndex = 0;
			DrawerActivity.mFocus_drawerChildPos = 0;
    		//remove preference 
			clearSharedPreferences(getActivity());			
			dialog.dismiss();
			getActivity().recreate();
		}
    };
    
    
    public static void clearSharedPreferences(Context ctx){
//    	String path = ctx.getFilesDir().getParent() + "/shared_prefs/";
//    	if(Util.isUriExisted(path, ctx))
    	{
	        File dir = new File(ctx.getFilesDir().getParent() + "/shared_prefs/");
	        String[] children = dir.list();
	        for (int i = 0; i < children.length; i++) {
	        	System.out.println("1: " + children[i]);
	            // clear each of the preferences
	            ctx.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
	        }
	        // Make sure it has enough time to save all the committed changes
	        try { Thread.sleep(1000); } catch (InterruptedException e) {}
	        for (int i = 0; i < children.length; i++) {
	        	System.out.println("2: " + children[i]);
	            // delete the files
	            new File(dir, children[i]).delete();
	        }
    	}
    }   
    
    // Show Licenses
    void showLicensesDlg()
    {
		View showLicenses = mRootView.findViewById(R.id.showLicenses);
		showLicenses.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				aboutLicenses();
			}
		});
    }
    
    // About licenses
	void aboutLicenses()
	{
	        final LicensesDialogFragment fragment = LicensesDialogFragment.newInstance(R.raw.notices, false);
	        fragment.show(getActivity().getSupportFragmentManager(), null);
	}
    
    // Show About
    void showAboutDlg()
    {
		View showAbout = mRootView.findViewById(R.id.showAbout);
		showAbout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				aboutDialog();
			}
		});
    }

	
    // About dialog
	void aboutDialog()
	{
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   PackageInfo pInfo = null;
		   String version_name = "NA";
		   int version_code = 0;
           try 
           {
        	   Context context = DrawerActivity.mDrawerActivity;
        	   pInfo = context.getPackageManager()
        			   		  .getPackageInfo(context.getPackageName(),PackageManager.GET_META_DATA);
           } catch (NameNotFoundException e) {
        	   e.printStackTrace();
           }

           if(pInfo != null)
           {
        	   version_name = pInfo.versionName;
        	   version_code = pInfo.versionCode;
           }
           String msgStr = getActivity().getResources().getString(R.string.config_about_version_name) + 
        		   			" : " + version_name + "\n" + 
        		   		   getActivity().getResources().getString(R.string.config_about_version_code) +
           					" : " + version_code + "\n\n" + 
   		   				   getActivity().getResources().getString(R.string.EULA_string);
           
		   builder.setTitle(R.string.config_about)
		   		  .setMessage(msgStr)
				  .setNegativeButton(R.string.btn_Cancel, null)
				  .show();
	}

}