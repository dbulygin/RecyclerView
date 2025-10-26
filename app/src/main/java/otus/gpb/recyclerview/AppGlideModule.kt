package otus.gpb.recyclerview

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Модуль Glide для оптимизации загрузки изображений
 * Устраняет предупреждение о GeneratedAppGlideModule
 */
@GlideModule
class AppGlideModule : AppGlideModule()
