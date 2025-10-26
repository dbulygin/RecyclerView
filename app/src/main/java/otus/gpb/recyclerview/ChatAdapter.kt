package otus.gpb.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import otus.gpb.recyclerview.databinding.ViewHolderGroupBinding
import otus.gpb.recyclerview.databinding.ViewHolderPersonBinding

class ChatAdapter(private val onItemClick: (Int) -> Unit) :
    ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (ViewTypes.fromId(viewType)) {
            ViewTypes.GROUP -> {
                val binding = ViewHolderGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                GroupChatViewHolder(binding)
            }
            ViewTypes.PERSON -> {
                val binding = ViewHolderPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PersonChatViewHolder(binding)
            }
            null -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupChat -> ViewTypes.GROUP.id
            is PersonChat -> ViewTypes.PERSON.id
            else -> throw IllegalArgumentException("Unknown item type: ${getItem(position)}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // привязка данных с проверкой типа и границ
        if (position < 0 || position >= itemCount) return
        
        try {
            when (holder) {
                is GroupChatViewHolder -> {
                    val item = getItem(position)
                    if (item is GroupChat) {
                        holder.bind(item)
                    }
                }
                is PersonChatViewHolder -> {
                    val item = getItem(position)
                    if (item is PersonChat) {
                        holder.bind(item)
                    }
                }
            }
            resetViewAppearance(holder.itemView)
        } catch (e: Exception) {
            // логируем ошибку и сбрасываем внешний вид View
            e.printStackTrace()
            resetViewAppearance(holder.itemView)
        }
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < currentList.size) {
            val newList = currentList.toMutableList().apply { removeAt(position) }
            submitList(newList)
        }
    }

    private fun resetViewAppearance(view: View) {
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
    }

    inner class GroupChatViewHolder(private val binding: ViewHolderGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GroupChat) {
            // кэшируем часто используемые View
            with(binding) {
                // Аватар с оптимизацией
                loadAvatar(item.avatarUrl, imgGroupAvatar, R.drawable.group_badge)
                // бейджи и иконки
                imgVoipBadgeContainer.visibility = item.voip.toVisibility()
                imgVerifiedIcon.visibility = item.verified.toVisibility()
                imgMuteIcon.visibility = item.muted.toVisibility()
                // тексты
                txtGroupName.text = item.groupName
                txtLastAuthor.text = item.lastAuthor
                txtLastMessage.text = item.lastMessage
                txtTimeValue.text = item.time
                // preview сообщения
                setupMessagePreview(item.messagePreviewUrl, imgMessagePreviewContainer, imgMessagePreview)
                // статусы прочтения
                setupReadStatus(item.checked, item.read, imgCheckedIcon, imgReadIcon)
                // счетчик
                setupCounter(item.counter, imgCounterContainer, txtCounterContainer)
                // дополнительные иконки
                imgPinnedIcon.visibility = item.pinned.toVisibility()
                imgMentionIconContainer.visibility = item.mentioned.toVisibility()

                root.setOnClickListener { onItemClick(item.id) }
            }
        }
    }

    inner class PersonChatViewHolder(private val binding: ViewHolderPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PersonChat) {
            with(binding) {
                // аватар
                loadAvatar(item.avatarUrl, imgPersonAvatar, R.drawable.person_badge)
                // бейджи статусов
                setupPersonBadges(item.checkbox, item.online, item.locked)
                // тексты
                txtPersonName.text = item.personName
                txtLastMessage.text = item.lastMessage
                txtTimeValue.text = item.time
                // иконки верификации и scam
                setupVerificationAndScam(item.verified, item.scam)
                imgMuteIcon.visibility = item.muted.toVisibility()
                // preview сообщения с оптимизацией
                setupMessagePreview(item.messagePreviewUrl, imgMessagePreviewContainer, imgMessagePreview)
                // статусы прочтения
                setupReadStatus(item.checked, item.read, imgCheckedIcon, imgReadIcon)
                // счётчик
                setupCounter(item.counter, imgCounterContainer, txtCounterContainer)
                // дополнительные иконки
                imgPinnedIcon.visibility = item.pinned.toVisibility()
                imgMentionIconContainer.visibility = item.mentioned.toVisibility()

                root.setOnClickListener { onItemClick(item.id) }
            }
        }

        private fun setupPersonBadges(checkbox: Boolean, online: Boolean, locked: Boolean) {
            with(binding) {
                imgCheckboxBadgeContainer.visibility = (checkbox && !online).toVisibility()
                imgOnlineBadgeContainer.visibility = online.toVisibility()
                imgLockedIcon.visibility = locked.toVisibility()
            }
        }

        private fun setupVerificationAndScam(verified: Boolean, scam: Boolean) {
            with(binding) {
                imgScamPatch.visibility = scam.toVisibility()
                imgVerifiedIcon.visibility = (verified && !scam).toVisibility()
            }
        }
    }

    // extension functions для загрузки изображений (эксперимент с оптимизацией)
    private fun loadAvatar(avatarUrl: String, imageView: ImageView, placeholder: Int) {
        if (avatarUrl.isNotEmpty()) {
            Glide.with(imageView.context)
                .load(avatarUrl)
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder) // Добавляем error placeholder
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL) // Кэшируем все
                .skipMemoryCache(false) // Используем память для быстрого доступа
                .thumbnail(0.1f) // Показываем уменьшенную версию сначала
                .into(imageView)
        } else {
            imageView.setImageResource(placeholder)
        }
    }

    private fun setupMessagePreview(previewUrl: String, container: View, imageView: ImageView) {
        if (previewUrl.isNotEmpty()) {
            container.visibility = View.VISIBLE
            Glide.with(imageView.context)
                .load(previewUrl)
                .centerCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .thumbnail(0.1f)
                .into(imageView)
        } else {
            container.visibility = View.GONE
        }
    }

    private fun setupReadStatus(checked: Boolean, read: Boolean, checkedIcon: ImageView, readIcon: ImageView) {
        checkedIcon.visibility = (checked && !read).toVisibility()
        readIcon.visibility = read.toVisibility()
    }

    private fun setupCounter(counter: Int, container: View, counterText: TextView) {
        if (counter == 0) {
            container.visibility = View.GONE
        } else {
            container.visibility = View.VISIBLE
            counterText.text = counter.toString()
        }
    }

    private fun Boolean.toVisibility() = if (this) View.VISIBLE else View.GONE

    enum class ViewTypes(val id: Int) {
        GROUP(R.layout.view_holder_group),
        PERSON(R.layout.view_holder_person);

        companion object {
            fun fromId(id: Int) = ViewTypes.entries.find { it.id == id }
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        // сравнение содержимого для избежания лишних перерисовок
        return when {
            oldItem is GroupChat && newItem is GroupChat -> {
                oldItem.groupName == newItem.groupName &&
                oldItem.lastAuthor == newItem.lastAuthor &&
                oldItem.lastMessage == newItem.lastMessage &&
                oldItem.avatarUrl == newItem.avatarUrl &&
                oldItem.messagePreviewUrl == newItem.messagePreviewUrl &&
                oldItem.voip == newItem.voip &&
                oldItem.verified == newItem.verified &&
                oldItem.muted == newItem.muted &&
                oldItem.time == newItem.time &&
                oldItem.checked == newItem.checked &&
                oldItem.read == newItem.read &&
                oldItem.mentioned == newItem.mentioned &&
                oldItem.pinned == newItem.pinned &&
                oldItem.counter == newItem.counter
            }
            oldItem is PersonChat && newItem is PersonChat -> {
                oldItem.personName == newItem.personName &&
                oldItem.lastMessage == newItem.lastMessage &&
                oldItem.avatarUrl == newItem.avatarUrl &&
                oldItem.messagePreviewUrl == newItem.messagePreviewUrl &&
                oldItem.checkbox == newItem.checkbox &&
                oldItem.online == newItem.online &&
                oldItem.locked == newItem.locked &&
                oldItem.scam == newItem.scam &&
                oldItem.verified == newItem.verified &&
                oldItem.muted == newItem.muted &&
                oldItem.time == newItem.time &&
                oldItem.checked == newItem.checked &&
                oldItem.read == newItem.read &&
                oldItem.mentioned == newItem.mentioned &&
                oldItem.pinned == newItem.pinned &&
                oldItem.counter == newItem.counter
            }
            else -> false
        }
    }
}