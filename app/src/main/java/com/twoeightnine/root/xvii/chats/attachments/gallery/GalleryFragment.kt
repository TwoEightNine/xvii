/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragmentForResult
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.cropper.ImageCropperFragment
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.ImageUtils
import com.twoeightnine.root.xvii.utils.PermissionHelper
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.time
import global.msnthrp.xvii.uikit.extensions.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_gallery_new.*
import java.io.File
import javax.inject.Inject

class GalleryFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseAttachViewModel.Factory
    private lateinit var viewModel: GalleryViewModel

    private val onlyPhotos by lazy {
        arguments?.getBoolean(ARG_ONLY_PHOTOS) == true
    }

    private val imageUtils by lazy {
        ImageUtils(activity ?: throw IllegalStateException("Where is activity?"))
    }

    private val adapter by lazy {
        GalleryAdapter(requireContext(), ::loadMore, ::onItemClick)
    }

    private val permissionHelper by lazy {
        PermissionHelper(this)
    }

    private val cropMapping = mutableMapOf<String, String>()

    override fun getLayoutId() = R.layout.fragment_gallery_new

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[GalleryViewModel::class.java]
        viewModel.onlyPhotos = onlyPhotos
        initRecycler()
        reloadData()
        adapter.startLoading()

        progressBar.show()
        swipeRefresh.setOnRefreshListener { reloadData() }

        fabDone.setOnClickListener { onDoneClicked() }

        rlPermissions.setVisible(!permissionHelper.hasStoragePermissions())
        rlPermissions.setOnClickListener {
            permissionHelper.request(arrayOf(PermissionHelper.READ_STORAGE, PermissionHelper.WRITE_STORAGE)) {
                rlPermissions.hide()
                progressBar.show()
                reloadData()
            }
        }

        if (!onlyPhotos) {
            llButtons.show()
            btnCamera.setOnClickListener {
                onCameraClick()
            }
            btnDoc.setOnClickListener {
                imageUtils.dispatchSelectFile(this)
            }
            rvAttachments.setPadding(0, resources.getDimensionPixelOffset(R.dimen.toolbar_height), 0, 0)
        }

        rvAttachments.applyBottomInsetPadding()
        fabDone.applyBottomInsetMargin()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getAttach().observe(viewLifecycleOwner, ::updateList)
    }

    private fun onDoneClicked() {
        val finalItems = adapter.multiSelect
                .map { deviceItem ->
                    if (deviceItem.path in cropMapping) {
                        deviceItem.copy(path = cropMapping[deviceItem.path] ?: deviceItem.path)
                    } else {
                        deviceItem
                    }
                }
        selectedSubject.onNext(finalItems)
        adapter.clearMultiSelect()
    }

    private fun reloadData() {
        if (permissionHelper.hasStoragePermissions()) {
            adapter.reset()
            adapter.startLoading()
            viewModel.loadAttach()
        } else {
            rlPermissions.show()
            progressBar.hide()
        }
    }

    private fun updateList(data: Wrapper<ArrayList<DeviceItem>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadAttach(offset)
    }

    private fun initRecycler() {
        rvAttachments.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        rvAttachments.adapter = adapter
        adapter.multiSelectMode = true
        adapter.multiListener = fabDone::setVisible
    }

    private fun onItemClick(deviceItem: DeviceItem) {
        startFragmentForResult<ImageCropperFragment>(REQUEST_CODE_CROP, ImageCropperFragment.createArgs(deviceItem.path))
    }

    private fun onCameraClick() {
        permissionHelper.doOrRequest(
                PermissionHelper.CAMERA,
                R.string.camera_permissions_title,
                R.string.camera_permissions_message
        ) { imageUtils.dispatchTakePictureIntent(this) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) return

        when (requestCode) {
            ImageUtils.REQUEST_TAKE_PHOTO, ImageUtils.REQUEST_SELECT_FILE -> {
                val path = imageUtils.getPath(requestCode, data)
                if (path != null && File(path).length() != 0L) {
                    val type = if (requestCode == ImageUtils.REQUEST_SELECT_FILE) {
                        DeviceItem.Type.DOC
                    } else {
                        DeviceItem.Type.PHOTO
                    }
                    selectedSubject.onNext(arrayListOf(DeviceItem(time() * 1000L, path, type)))
                    adapter.clearMultiSelect()
                } else {
                    L.tag("camera")
                            .warn()
                            .log("path is empty but request code is $requestCode and data = $data")
                    showError(context, R.string.unable_to_pick_file)
                }
            }
            REQUEST_CODE_CROP -> {
                val origPath = data?.extras?.getString(ImageCropperFragment.RESULT_ORIG_PATH)
                        ?: return
                val croppedPath = data.extras?.getString(ImageCropperFragment.RESULT_CROPPED_PATH)
                        ?: return
                cropMapping[origPath] = croppedPath
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter.checkSelected(origPath)
                }, 300L)
            }
        }
    }

    companion object {

        private const val REQUEST_CODE_CROP = 8267
        private const val ARG_ONLY_PHOTOS = "onlyPhotos"

        const val SPAN_COUNT = 4

        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<List<DeviceItem>>()

        fun newInstance(onlyPhotos: Boolean = false, onSelected: (List<DeviceItem>) -> Unit): GalleryFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return GalleryFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_ONLY_PHOTOS, onlyPhotos)
                }
            }
        }

        fun clear() {
            disposables.clear()
        }
    }
}