package otus.gpb.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import otus.gpb.recyclerview.databinding.ActivityMainBinding
import kotlin.random.Random
import androidx.core.graphics.drawable.toDrawable
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private var chatItems: List<ChatItem> = emptyList()
    
    // Кэшируем Random для оптимизации производительности
    private val random = Random(System.currentTimeMillis())
    
    // Счетчик для генерации уникальных ID
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.addItemDecoration(CustomDecorator(this).apply {
            setColor(R.color.color_chat_divider_light)
            setOffset(R.integer.dividerOffset)
        })

        // Настройка RecyclerView с оптимизацией производительности
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Настройка RecycledViewPool для переиспользования ViewHolder
        val viewPool = RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(ChatAdapter.ViewTypes.GROUP.id, 15) // Увеличиваем пул для групп
            setMaxRecycledViews(ChatAdapter.ViewTypes.PERSON.id, 15) // Увеличиваем пул для персон
        }
        binding.recyclerView.setRecycledViewPool(viewPool)
        
        // Включаем оптимизацию для стабильных ID
        binding.recyclerView.setHasFixedSize(true)
        
        // Отключаем анимацию по умолчанию для лучшей производительности
        binding.recyclerView.itemAnimator = null
        
        // Включаем кэширование для лучшей производительности
        binding.recyclerView.setItemViewCacheSize(20)
        
        adapter = ChatAdapter() { id ->
            println(id)
        }

        binding.recyclerView.adapter = adapter
        ItemTouchHelper(
            ItemTouchHelperCallback(
                adapter,
                this
            )
        ).attachToRecyclerView(binding.recyclerView)



        // Инициализируем nextId после загрузки начальных данных
        chatItems = generateList(isInitial = true)
        nextId = chatItems.maxOfOrNull { it.id }?.plus(1) ?: 1
        adapter.submitList(chatItems)

        // Оптимизированный scroll listener для предотвращения пропуска кадров
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var isLoading = false
            private val pageSize = 20 // Размер страницы для пагинации
            
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                // Проверяем только при прокрутке вниз и если не загружаем уже
                if (dy > 0 && !isLoading) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisible = layoutManager.findLastVisibleItemPosition()

                    // Загружаем новые элементы заранее, когда остается 5 элементов
                    if (lastVisible >= totalItemCount - 5) {
                        isLoading = true
                        loadMoreItems()
                    }
                }
            }
            
            private fun loadMoreItems() {
                // Используем post для выполнения в следующем цикле UI
                binding.recyclerView.post {
                    val newItems = generateList(isInitial = false)
                    chatItems = chatItems + newItems
                    adapter.submitList(chatItems) {
                        isLoading = false
                    }
                }
            }
        })
    }

    private fun getRandomBool() = random.nextInt(0, 2) > 0

    @SuppressLint("DefaultLocale")
    private fun getRandomTime(): String {
        val hour = random.nextInt(0, 24)
        val min = random.nextInt(0, 60)
        return String.format("%d:%02d", hour, min)
    }

    private fun generateRandomMessage(): String {
        val messages = listOf(
            "Привет! Как дела?",
            "Посмотри на это фото",
            "Что думаешь об этом?",
            "Интересная новость",
            "Встречаемся завтра?",
            "Спасибо за помощь",
            "Отличная идея!",
            "Не могу поверить",
            "Это потрясающе!",
            "Давай обсудим",
            "Нужна твоя помощь",
            "Как прошла встреча?",
            "Всё готово",
            "Отлично получилось",
            "Не забудь про завтра"
        )
        return messages[random.nextInt(messages.size)]
    }

    private fun randomizeGroupChat(item: GroupChat): GroupChat {
        return GroupChat(
            id = nextId++, // Генерируем уникальный ID
            groupName = "${item.groupName} ${random.nextInt(1, 1000)}", // Добавляем номер для уникальности
            lastAuthor = item.lastAuthor,
            lastMessage = generateRandomMessage(),
            avatarUrl = item.avatarUrl,
            messagePreviewUrl = if (random.nextInt(0, 3) == 0) item.messagePreviewUrl else "",
            voip = getRandomBool(),
            verified = getRandomBool(),
            muted = getRandomBool(),
            time = getRandomTime(),
            checked = getRandomBool(),
            read = getRandomBool(),
            mentioned = getRandomBool(),
            pinned = getRandomBool(),
            counter = if (random.nextInt(0, 10) > 0) 0 else {
                random.nextInt(1, 199)
            }
        )
    }

    private fun randomizePersonChat(item: PersonChat): PersonChat {
        return PersonChat(
            id = nextId++, // Генерируем уникальный ID
            personName = "${item.personName} ${random.nextInt(1, 1000)}", // Добавляем номер для уникальности
            lastMessage = generateRandomMessage(),
            avatarUrl = item.avatarUrl,
            messagePreviewUrl = if (random.nextInt(0, 3) == 0) item.messagePreviewUrl else "",
            checkbox = getRandomBool(),
            online = getRandomBool(),
            locked = getRandomBool(),
            scam = if (random.nextInt(0, 10) > 0) false else getRandomBool(),
            verified = getRandomBool(),
            muted = getRandomBool(),
            time = getRandomTime(),
            checked = getRandomBool(),
            read = getRandomBool(),
            mentioned = getRandomBool(),
            pinned = getRandomBool(),
            counter = if (random.nextInt(0, 10) > 0) 0 else {
                random.nextInt(1, 19)
            }
        )
    }

    private fun generateList(isInitial: Boolean = false): List<ChatItem> {
        val pairs = minOf(personChatList.size, groupChatList.size)

        return if (isInitial) {
            // Для начальной загрузки используем оригинальные данные
            (0 until pairs).flatMap { i ->
                listOf(personChatList[i], groupChatList[i])
            }
        } else {
            // Для пагинации генерируем новые элементы с уникальными ID
            (0 until pairs).flatMap { i ->
                listOf(
                    randomizePersonChat(personChatList[i]),
                    randomizeGroupChat(groupChatList[i])
                )
            }
        }
    }

    class ItemTouchHelperCallback(
        private val adapter: ChatAdapter,
        private val context: Context
    ) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private var background: ColorDrawable? = null
        private var archiveIcon: Drawable? = null
        private var textPaint: Paint = Paint().apply {
            textSize = 40f
            color = ContextCompat.getColor(context, android.R.color.white)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                adapter.removeItem(position)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            if (background == null) {
                background = ContextCompat.getColor(context, R.color.color_chat_background_light)
                    .toDrawable()
            }

            if (archiveIcon == null) {
                archiveIcon = ContextCompat.getDrawable(context, R.drawable.icon_archive)
            }

            if (dX < 0) { // Свайп влево
                // Рисуем подложку
                background?.apply {
                    setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    draw(c)
                }

                if (abs(dX) > 0) {
                    archiveIcon?.apply {
                        val iconMargin = (itemHeight - intrinsicHeight) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + intrinsicHeight
                        val iconLeft = itemView.right - iconMargin - intrinsicWidth
                        val iconRight = itemView.right - iconMargin

                        setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        draw(c)

                        val textX = (iconLeft + iconRight) / 2f // Центр иконки по X
                        val textY = iconBottom + 40f // Под иконкой с отступом

                        c.drawText(
                            context.getString(R.string.archive_text_for_icon),
                            textX,
                            textY,
                            textPaint
                        )
                    }
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }


}