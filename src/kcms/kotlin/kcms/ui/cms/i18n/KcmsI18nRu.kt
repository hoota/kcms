package kcms.ui.cms.i18n

import org.springframework.stereotype.Component

@Component
class KcmsI18nRu : KcmsI18n {
    override val more: String
        get() = "Ещё..."
    override val newValues: String
        get() = "Новые значения (по одному на строке)"
    override val enumCategory: String
        get() = "Перечисление"
    override val value: String
        get() = "Значение"
    override val close: String
        get() = "Закрыть"
    override val edit: String
        get() = "Редактировать"
    override val save: String
        get() = "Сохранить"
    override val moveToTop: String
        get() = "Передвинуть на самый верх"
    override val moveUp: String
        get() = "Передвинуть повыше"
    override val moveDown: String
        get() = "Перевинуть пониже"
    override val moveToTheBottom: String
        get() = "Передвинуть в самый низ"
    override val pages: String
        get() = "Страницы"
    override val settings: String
        get() = "Настройки"
    override val files: String
        get() = "Файлы"
    override val backgroundJobs: String
        get() = "Фоновые задачи"
    override val enums: String
        get() = "Перечисления"
    override val addCategory: String
        get() = "Добавить категорию"
    override val category: String
        get() = "Категория"
    override val enumCategories: String
        get() = "Категории перечислений"
    override val sharedFiles: String
        get() = "Общие файлы"
    override val jobName: String
        get() = "Название задачи"
    override val status: String
        get() = "Статус"
    override val runJob: String
        get() = "Запустить задачу"
    override val page: String
        get() = "Страница"
    override val properties: String
        get() = "Свойства"
    override val children: String
        get() = "Дочерние страницы"
    override val title: String
        get() = "Заголовок"
    override val template: String
        get() = "Шаблон"
    override val published: String
        get() = "Опубликовано"
    override val yes: String
        get() = "да"
    override val no: String
        get() = "нет"
    override val newPage: String
        get() = "Новая Страница"
    override val saveAndContinue: String
        get() = "Сохранить и продолжить редактирование"
    override val removePage: String
        get() = "Удалить страницу"
    override val parent: String
        get() = "Родительская страница"
    override val pageSlug: String
        get() = "Адрес в строке браузера"
    override val noParent: String
        get() = "без"
    override val searchQuery: String
        get() = "Поисковый запрос"
    override val searchInContent: String
        get() = "искать в свойствах"
    override val anyTemplate: String
        get() = "любой шаблон"
    override val anyParent: String
        get() = "любой родитель"
    override val search: String
        get() = "Искать"
    override val upload: String
        get() = "Загрузить"
    override val fileUrl: String
        get() = "Ссылка на файл"
    override val chooseFile: String
        get() = "Выбрать файл"
    override val remove: String
        get() = "удалить"
    override val areYouSure: String
        get() = "Вы уверены?"
}