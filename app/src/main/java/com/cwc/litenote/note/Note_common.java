/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cwc.litenote.note;

import java.util.Date;

import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.image.UtilImage_bitmapLoader;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Note_common {

	static TextView mAudioTextView;
    
    static ImageView mPicImageView;
    static String mPictureUriInDB;
    static String mAudioUriInDB;
    static String mLinkUriInDB;
    static String mOriginalPictureUri;
    static String mCurrentPictureUri;
    static String mCurrentAudioUri;

    static String mOriginalAudioUri;
    static String mOriginalDrawingUri;
    static String mOriginalLinkUri;

    static EditText mLinkEditText;
    static EditText mTitleEditText;
    static EditText mBodyEditText;
    static String mOriginalTitle;
    static String mOriginalBody;
    
    Long mRowId;
	static Long mOriginalCreatedTime;
	static Long mOriginalMarking;
    
    static boolean bRollBackData;
    static boolean bRemovePictureUri = false;
    static boolean bRemoveAudioUri = false;
    boolean bEditPicture = false;

    private static DB mDb;
    SharedPreferences mPref_style;
    SharedPreferences mPref_delete_warn;
    static Activity mAct;
    static int mStyle;
    static ProgressBar progressBar;
    static ProgressBar progressBarExpand;
    
    public Note_common(Activity act,Long rowId,String strTitle, String pictureUri, String audioUri, String drawingUri, String linkUri, String strBody, Long createdTime)
    {
    	mAct = act;
    	mRowId = rowId;
    			
    	mOriginalTitle = strTitle;
	    mOriginalBody = strBody;
	    mOriginalPictureUri = pictureUri;
	    mOriginalAudioUri = audioUri;
	    mOriginalDrawingUri = drawingUri;
	    mOriginalLinkUri = linkUri;
	    
	    mOriginalCreatedTime = createdTime;
	    mCurrentPictureUri = pictureUri;
	    mCurrentAudioUri = audioUri;
	    
	    mDb = NoteFragment.mDb_notes;
	    
	    mOriginalMarking = mDb.getNoteMarkingById(rowId);
		
	    bRollBackData = false;
		bEditPicture = true;
		bShowEnlargedImage = false;
    }
    
    public Note_common(Activity act)
    {
    	mAct = act;
    	mDb = NoteFragment.mDb_notes;
    }
    
    void UI_init()
    {
    	mAudioTextView = (TextView) mAct.findViewById(R.id.edit_audio);
    	mLinkEditText = (EditText) mAct.findViewById(R.id.edit_link);
    	mTitleEditText = (EditText) mAct.findViewById(R.id.edit_title);
        mBodyEditText = (EditText) mAct.findViewById(R.id.edit_body);
        mPicImageView = (ImageView) mAct.findViewById(R.id.edit_picture);

        progressBar = (ProgressBar) mAct.findViewById(R.id.edit_progress_bar);
        progressBarExpand = (ProgressBar) mAct.findViewById(R.id.edit_progress_bar_expand);
        		
		mStyle = TabsHostFragment.mDbTabs.getTabStyle(TabsHostFragment.mCurrent_tabIndex, true);

		//set audio color
//		mAudioTextView.setTextColor(Util.mText_ColorArray[style]);
//		mAudioTextView.setBackgroundColor(Util.mBG_ColorArray[style]);
		
		//set link color
		if(mLinkEditText != null)
		{
			mLinkEditText.setTextColor(Util.mText_ColorArray[mStyle]);
			mLinkEditText.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		}
		
		//set title color
		mTitleEditText.setTextColor(Util.mText_ColorArray[mStyle]);
		mTitleEditText.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		
		mPicImageView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
		
		//set body color 
		mBodyEditText.setTextColor(Util.mText_ColorArray[mStyle]);
		mBodyEditText.setBackgroundColor(Util.mBG_ColorArray[mStyle]);	
		
	    final InputMethodManager imm = (InputMethodManager)mAct.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		// set thumb nail listener
        mPicImageView.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View view) {
            	if(bShowEnlargedImage == true)
            	{
            		closeEnlargedImage();
            		// show soft input
            		if (mAct.getCurrentFocus() != null)   
            		    imm.showSoftInput(mAct.getCurrentFocus(), 0);
            	}	
            	else
                {
            		// hide soft input
            		if (mAct.getCurrentFocus() != null) 		    
            			imm.hideSoftInputFromWindow(mAct.getCurrentFocus().getWindowToken(), 0);
            		
                	System.out.println("Note_common / mPictureUriInDB = " + mPictureUriInDB);
                	if(!Util.isEmptyString(mPictureUriInDB))
                	{
                		bRemovePictureUri = false;
                		System.out.println("mPicImageView.setOnClickListener / mPictureUriInDB = " + mPictureUriInDB);
                		
                		// check if pictureUri has scheme
                		if(Util.isUriExisted(mPictureUriInDB, mAct))
                		{
	                		if(Uri.parse(mPictureUriInDB).isAbsolute())
	                		{
	                			new UtilImage_bitmapLoader(Note_edit.mEnlargedImage, mPictureUriInDB, progressBarExpand, 
	                					(NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light: UilCommon.optionsForRounded_dark), mAct);
	                			bShowEnlargedImage = true;
	                		}
	                		else
	                		{
	                			System.out.println("mPictureUriInDB is not Uri format");
	                		}
                		}
                		else
                			Toast.makeText(mAct,R.string.file_not_found,Toast.LENGTH_SHORT).show();
                	}
                	else
            			Toast.makeText(mAct,R.string.file_is_not_created,Toast.LENGTH_SHORT).show();

				} 
            }
        });
        
		// set thumb nail long click listener
        mPicImageView.setOnLongClickListener(new View.OnLongClickListener() 
        {
            @Override
            public boolean onLongClick(View view) {
            	if(bEditPicture)
            		openSetPictureDialog();
                return false;
            }
        });
    }
    
    // set image close listener
    static void setCloseImageListeners(EditText editText)
    {
    	editText.setOnClickListener(new OnClickListener()
    	{   @Override
			public void onClick(View v) 
			{
				if(bShowEnlargedImage == true)
					closeEnlargedImage();
			}
		});
    	
    	editText.setOnFocusChangeListener(new OnFocusChangeListener() 
    	{   @Override
            public void onFocusChange(View v, boolean hasFocus) 
    		{
    				if(bShowEnlargedImage == true)
    					closeEnlargedImage();
            } 
    	});   
    }
    
    
    static boolean bShowEnlargedImage;
    public static void closeEnlargedImage()
    {
    	System.out.println("closeExpandImage");
		Note_edit.mEnlargedImage.setVisibility(View.GONE);
		bShowEnlargedImage = false;
    }
    
    void openSetPictureDialog() 
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
		builder.setTitle(R.string.edit_note_set_picture_dlg_title)
			   .setMessage(mCurrentPictureUri)	
			   .setNeutralButton(R.string.btn_Select, new DialogInterface.OnClickListener()
			   {
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						bRemovePictureUri = false; // reset
						// For selecting local gallery
//						Intent intent = new Intent(mAct, PictureGridAct.class);
//						intent.putExtra("gallery", false);
//						mAct.startActivityForResult(intent, Util.ACTIVITY_SELECT_PICTURE);
						
						// select global
						final String[] items = new String[]{mAct.getResources().getText(R.string.note_ready_image).toString(),
															mAct.getResources().getText(R.string.note_ready_video).toString()};
					    AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
					   
					    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
					    {
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								String mediaType = null;
								if(which ==0)
									mediaType = "image/*";
								else if(which ==1)
									mediaType = "video/*";
								
								System.out.println("Note_common / _openSetPictureDialog / mediaType = " + mediaType);
								mAct.startActivityForResult(Util.chooseMediaIntentByType(mAct, mediaType),
				   						Util.CHOOSER_SET_PICTURE);	
								//end
								dialog.dismiss();
							}
					    };
					    builder.setTitle(R.string.view_note_mode_picture)
							   .setSingleChoiceItems(items, -1, listener)
							   .setNegativeButton(R.string.btn_Cancel, null)
							   .show();
					}
				})					
			   .setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener()
			   {
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{// cancel
					}
				});

				if(!mPictureUriInDB.isEmpty())
				{
					builder.setPositiveButton(R.string.btn_None, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							//just delete picture file name
							mCurrentPictureUri = "";
							mOriginalPictureUri = "";
					    	removePictureStringFromCurrentEditNote(mRowId);
					    	populateFields(mRowId);
					    	bRemovePictureUri = true;
						}
					});
				}
		
		Dialog dialog = builder.create();
		dialog.show();
    }
    
    static void deleteNote(Long rowId)
    {
    	System.out.println("Note_common / _deleteNote");
        // for Add new note (mRowId is null first), but decide to cancel 
        if(rowId != null)
        	mDb.deleteNote(rowId,true);
    }
    

    
    static void populateFields(Long rowId) 
    {
    	if (rowId != null) 
    	{
    		// for picture block
    		mPictureUriInDB = mDb.getNotePictureUriById(rowId);
			System.out.println("populateFields / mPictureFileNameInDB = " + mPictureUriInDB);
    		
			// load bitmap to image view
			if(!Util.isEmptyString(mPictureUriInDB))
			{
				new UtilImage_bitmapLoader(mPicImageView, mPictureUriInDB, progressBar, 
    					(NoteFragment.mStyle % 2 == 1 ? UilCommon.optionsForRounded_light: UilCommon.optionsForRounded_dark), mAct);
			}
			else
			{
	    		mPicImageView.setImageResource(mStyle%2 == 1 ?
		    			R.drawable.btn_radio_off_holo_light:
		    			R.drawable.btn_radio_off_holo_dark);
			}
			
			// set listeners for closing image view 
	    	if(!Util.isEmptyString(mPictureUriInDB))
	    	{
	    		Note_common.setCloseImageListeners(Note_common.mLinkEditText); 	
	    		Note_common.setCloseImageListeners(Note_common.mTitleEditText); 	
	    		Note_common.setCloseImageListeners(Note_common.mBodyEditText);
	    	}			
	    	
    		// audio
			mAudioUriInDB = mDb.getNoteAudioUriById(rowId);
        	if(!Util.isEmptyString(mAudioUriInDB))
    		{
//    			String audio_name = Util.getDisplayNameByUriString(mAudioUriInDB,mAct);
    			String audio_name = mAudioUriInDB;
				System.out.println("populateFields / set audio name / audio_name = " + audio_name);
				mAudioTextView.setText(mAct.getResources().getText(R.string.note_audio) + ": " + audio_name);
    		}
        	else
				mAudioTextView.setText("");
        		
    		// link
			String strLinkEdit = mDb.getNoteLinkById(rowId);
            mLinkEditText.setText(strLinkEdit);
            mLinkEditText.setSelection(strLinkEdit.length());

            // title        	
			String strTitleEdit = mDb.getNoteTitleById(rowId);
			String curLinkStr = mLinkEditText.getText().toString();
			final String hint = Util.getYoutubeTitle(curLinkStr);
			if( Util.isEmptyString(strTitleEdit) &&
				Util.isEmptyString(mTitleEditText.getText().toString()) &&	
				Util.isYouTubeLink(curLinkStr)        )
			{
				mTitleEditText.setHint(Html.fromHtml("<small style=\"text-color: gray;\"><i>" +
													 hint + 
													 "</i></small>"));
				mTitleEditText.setOnFocusChangeListener(new OnFocusChangeListener()
				{
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus)
						{
							((EditText)v).setText(hint);
							((EditText)v).setSelection(hint.length());
						}
					}
				});
			}
			else
			{
				mTitleEditText.setText(strTitleEdit);
				mTitleEditText.setSelection(strTitleEdit.length());
			}
			

            // body
            String strBodyEdit = mDb.getNoteBodyById(rowId);
            mBodyEditText.setText(strBodyEdit);
            mBodyEditText.setSelection(strBodyEdit.length());
        }
    	else
    	{
            // renew link
			String strLinkEdit = "";
			if(mLinkEditText != null)
			{
	            mLinkEditText.setText(strLinkEdit);
	            mLinkEditText.setSelection(strLinkEdit.length());
	            mLinkEditText.requestFocus();
			}
			
    		// renew title
			String strTitleEdit = "";
            mTitleEditText.setText(strTitleEdit);
            mTitleEditText.setSelection(strTitleEdit.length());
            mTitleEditText.requestFocus();
            
            // renew body
            String strBodyEdit = "";
            mBodyEditText.setText(strBodyEdit);
            mBodyEditText.setSelection(strBodyEdit.length());
    	}
    }
    
    boolean isLinkUriModified()
    {
    	return !mOriginalLinkUri.equals(mLinkEditText.getText().toString());
    }    

    boolean isTitleModified()
    {
    	return !mOriginalTitle.equals(mTitleEditText.getText().toString());
    }
    
    boolean isPictureModified()
    {
    	return !mOriginalPictureUri.equals(mPictureUriInDB);
    }
    
    boolean isAudioModified()
    {
    	if(mOriginalAudioUri == null)
    		return false;
    	else
    		return !mOriginalAudioUri.equals(mAudioUriInDB);
    }    
    
    boolean isBodyModified()
    {
    	return !mOriginalBody.equals(mBodyEditText.getText().toString());
    }
    
    boolean isTimeCreatedModified()
    {
    	return false; 
    }
    
    boolean isNoteModified()
    {
    	boolean bModified = false;
    	if( isTitleModified() ||
    		isPictureModified() ||
    		isAudioModified() ||
    		isBodyModified() ||
    		isLinkUriModified() ||
    		bRemovePictureUri ||
    		bRemoveAudioUri)
    	{
    		bModified = true;
    	}
    	
    	return bModified;
    }
    
    boolean isTextAdded()
    {
    	boolean bEdit = false;
    	String curTitle = mTitleEditText.getText().toString();
    	String curBody = mBodyEditText.getText().toString();
       	
    	if(!Util.isEmptyString(curTitle)||
       	   !Util.isEmptyString(curBody)   )
       	{
    		bEdit = true;
       	}
       	
    	return bEdit;
    }

	public static Long saveStateInDB(Long rowId,boolean enSaveDb, String pictureUri, String audioUri, String drawingUri) 
	{
		boolean mEnSaveDb = enSaveDb;
		String linkUri = "";
		if(mLinkEditText != null)
			linkUri = mLinkEditText.getText().toString();
    	String title = mTitleEditText.getText().toString();
    	String body = mBodyEditText.getText().toString();
    	
        if(mEnSaveDb)
        {
	        if (rowId == null) // for Add new
	        {
	        	if( (!title.isEmpty()) ||
	        		(!body.isEmpty()) ||
	        		(!pictureUri.isEmpty()) || 
	        		(!audioUri.isEmpty()) ||
	        		(!linkUri.isEmpty())            )
	        	{
	        		// insert
	        		System.out.println("Note_common / _saveStateInDB / insert");
	        		rowId = mDb.insertNote(title, pictureUri, audioUri, drawingUri, linkUri, body, 0, (long) 0);// add new note, get return row Id
	        	}
        		mCurrentPictureUri = pictureUri; // update file name
        		mCurrentAudioUri = audioUri; // update file name
	        } 
	        else // for Edit
	        {
    	        Date now = new Date(); 
	        	if( !Util.isEmptyString(title) || 
	        		!Util.isEmptyString(body) ||
	        		!Util.isEmptyString(pictureUri) ||
	        		!Util.isEmptyString(audioUri) ||
	        		!Util.isEmptyString(linkUri)       )
	        	{
	        		// update
	        		if(bRollBackData) //roll back
	        		{
			        	System.out.println("Note_common / _saveStateInDB / update: roll back");
			        	linkUri = mOriginalLinkUri;
	        			title = mOriginalTitle;
	        			body = mOriginalBody;
	        			Long time = mOriginalCreatedTime;
	        			mDb.updateNote(rowId, title, pictureUri, audioUri, drawingUri, linkUri, body, mOriginalMarking, time,true);
	        		}
	        		else // update new
	        		{
	        			System.out.println("Note_common / _saveStateInDB / update new");
	        			System.out.println("--- rowId = " + rowId);
	        			boolean isOK = false;
	        			isOK = mDb.updateNote(rowId, title, pictureUri, audioUri, drawingUri, linkUri, body, mOriginalMarking, now.getTime(),true); // update note
	        			System.out.println("--- isOK = " + isOK);
	        		}
	        		mCurrentPictureUri = pictureUri;
	        		mCurrentAudioUri = audioUri;
	        	}
	        	else if( Util.isEmptyString(title) &&
	        			 Util.isEmptyString(body) &&
			        	 Util.isEmptyString(pictureUri) &&
			        	 Util.isEmptyString(audioUri) &&
			        	 Util.isEmptyString(linkUri)         )
	        	{
	        		// delete
	        		System.out.println("Note_common / _saveStateInDB / delete");
	        		deleteNote(rowId);
	        	}
	        }
        }
        
		return rowId;
	}

	public static Long savePictureStateInDB(Long rowId,boolean enSaveDb, String pictureUri, String audioUri, String drawingUri, String linkUri) 
	{
		boolean mEnSaveDb = enSaveDb;
        if(mEnSaveDb)
        {
	        if (rowId == null) // for Add new
	        {
	        	if( !pictureUri.isEmpty())
	        	{
	        		// insert
	        		System.out.println("Note_common / _savePictureStateInDB / insert");
	        		rowId = mDb.insertNote("", pictureUri, audioUri, drawingUri, linkUri, "", 1, (long) 0);// add new note, get return row Id
	        	}
        		mCurrentPictureUri = pictureUri; // update file name
	        } 
	        else // for Edit
	        {
    	        Date now = new Date(); 
	        	if( !pictureUri.isEmpty())
	        	{
	        		// update
	        		if(bRollBackData) //roll back
	        		{
			        	System.out.println("Note_common / _savePictureStateInDB / update: roll back");
	        			Long time = mOriginalCreatedTime;
	        			mDb.updateNote(rowId, "", pictureUri, audioUri, drawingUri, linkUri, "", mOriginalMarking, time, true);
	        		}
	        		else // update new
	        		{
	        			System.out.println("Note_common / _savePictureStateInDB / update new");
	        			mDb.updateNote(rowId, "", pictureUri, audioUri, drawingUri, linkUri, "", 1, now.getTime(), true); // update note
	        		}
	        		mCurrentPictureUri = pictureUri; // update file name
	        	}
	        	else if(pictureUri.isEmpty())
	        	{
	        		// delete
	        		System.out.println("Note_common / _savePictureStateInDB / delete");
	        		deleteNote(rowId);
	        	}
	        }
        }
        
		return rowId;
	}
	
	// for confirmation condition
	public void removePictureStringFromOriginalNote(Long rowId) {
    	mDb.updateNote(rowId, 
    				   mOriginalTitle,
    				   "",
    				   mOriginalAudioUri,
    				   mOriginalDrawingUri,
    				   mOriginalLinkUri,
    				   mOriginalBody,
    				   mOriginalMarking,
    				   mOriginalCreatedTime, true );
	}
	
	public void removePictureStringFromCurrentEditNote(Long rowId) {
        String linkUri = mLinkEditText.getText().toString();
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();
        
    	mDb.updateNote(rowId, 
    				   title,
    				   "",
    				   mOriginalAudioUri,
    				   mOriginalDrawingUri,
    				   linkUri,
    				   body,
    				   mOriginalMarking,
    				   mOriginalCreatedTime, true );
	}
	
	public void removeAudioStringFromOriginalNote(Long rowId) {
    	mDb.updateNote(rowId, 
    				   mOriginalTitle,
    				   mOriginalPictureUri, 
    				   "",
    				   mOriginalDrawingUri,
    				   mOriginalLinkUri,    				   
    				   mOriginalBody,
    				   mOriginalMarking,
    				   mOriginalCreatedTime, true );
	}	
	
	public static void removeAudioStringFromCurrentEditNote(Long rowId) {
        String linkUri = mLinkEditText.getText().toString();
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();
        mDb.updateNote(rowId, 
    				   title,
    				   mOriginalPictureUri, 
    				   "",
    				   mOriginalDrawingUri,
    				   linkUri,
    				   body,
    				   mOriginalMarking,
    				   mOriginalCreatedTime, true );
	}	
	
	public static void removeLinkUriFromCurrentEditNote(Long rowId) {
        String title = mTitleEditText.getText().toString();
        String body = mBodyEditText.getText().toString();
        mDb.updateNote(rowId, 
    				   title,
    				   mOriginalPictureUri, 
    				   mOriginalAudioUri,
    				   mOriginalDrawingUri,
    				   "",
    				   body,
    				   mOriginalMarking,
    				   mOriginalCreatedTime, true );
	}	
	
	static int getCount()
	{
		int noteCount = mDb.getNotesCount(true);
		return noteCount;
	}
	
	// for audio
	public static Long insertAudioToDB(String audioUri) 
	{
		Long rowId = null;
       	if( !Util.isEmptyString(audioUri))
    	{
    		// insert
    		System.out.println("Note_common / _insertAudioToDB / insert");
    		// set marking to 1 for default
    		rowId = mDb.insertNote("", "", audioUri, "", "", "", 1, (long) 0);// add new note, get return row Id
    	}
		return rowId;
	}
	
}