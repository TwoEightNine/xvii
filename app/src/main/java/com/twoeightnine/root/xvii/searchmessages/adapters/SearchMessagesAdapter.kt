package com.twoeightnine.root.xvii.searchmessages.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.load
import kotlinx.android.synthetic.main.item_dialog_search.view.*

class SearchMessagesAdapter(context: Context,
                            loader: (Int) -> Unit,
                            private var listener: (Int) -> Unit,
                            private var longListener: (Int) -> Boolean) : PaginationAdapter<Message>(context, loader) {

    override fun createHolder(parent: ViewGroup, viewType: Int): SearchDialogViewHolder {
        return SearchDialogViewHolder(View.inflate(context, R.layout.item_dialog_search, null))
    }

    override fun onBindViewHolder(vholder: RecyclerView.ViewHolder, position: Int) {
        (vholder as? SearchDialogViewHolder)?.bind(items[position])
    }

    override var stubLoadItem: Message? = Message.stubLoad

    override fun isStubLoad(obj: Message) = Message.isStubLoad(obj)

    override var stubTryItem: Message? = Message.stubTry

    override fun isStubTry(obj: Message) = Message.isStubTry(obj)

    inner class SearchDialogViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            with(itemView) {
                civPhoto.load(message.photo)
                tvTitle.text = message.title
                ivOnlineDot.hide()
                Style.forImageView(ivOnlineDot, Style.MAIN_TAG)
                rlItemContainer.setOnClickListener { listener.invoke(adapterPosition) }
                rlItemContainer.setOnLongClickListener { longListener.invoke(adapterPosition) }
            }
        }

    }
}