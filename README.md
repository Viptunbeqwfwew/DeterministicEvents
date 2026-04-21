Deterministic Events
====================

Мод предназначен для конфигурации последовательности
обработчиков событий, приоритет которых вас не 
устраивает.

Как работает
------------

Мод отслеживает и внедряется в основную шину 
событий Forge, в MinecraftForge.EVENT_BUS.

Конфигурация мода
-----------------

Мод читает и парсит все файлы из директории
config/deterministicevents с расширением
*.dconf.

Для конфигурационных файлов используется 6 
ключевых слова (Комментарии доступны через "//"):

* order - модификатор флага, гарантирующий 
  строгую последовательность. Запрещает 
  дополнение параметров для объявленного объекта.

* reverse - модификатор параметров объектов, который
  буквально меняет местами имя объекта и его параметров.

* mute - блокирует вызов обработчиков, перед
  вхождением в целевой. Можно применить reverse.

* group - указывает на то, как интерпретировать
  данные после себя. Используется для
  объединения несколько обработчиков в одну
  субъединицу. Можно применить order.

* supergroup - указывает на то, в каком порядке
  располагать субъединицы групп. Только одно
  объявление на одно событие во всех файлах.
  Уничтожает дубликаты групп.

* next_phase - переключатель фазы, для события в
  supergroup (HIGHEST [auto], HIGH, NORMAL, LOW и LOWEST).
  Не является обязательным, но желательным.
  Всего может быть 4-ре объявления. Если больше, 
  supergroup сбросится до стандартного варианта. 
  Автоматически дополняет не достающие
  next_phase, с обеих сторон.

* default_slot - определяет место, куда попадут
  все обработчики, не вошедшие ни в одну из
  созданных групп. И все группы из родителей,
  место для которого вы не переопределили
  в текущей supergroup. Если отсутствует,
  автоматически вставится последним. Всего 5
  на супергруппу, по одному на фазу.

* contract - объявляет контракт для группы.
  Использует то же пространство имён, что и
  группы.

* condition - задаёт условие, при выполнении
  которых, вызов будет отложен.

* mapping - подтягивает данные из текущего 
  события в отложенное.

Если необходимо дополнить объект в другом месте. И он или его 
модификатор этого не запрещает. То используйте стандартную структуру
объявление.

В случаи возникновения конфликтов, объекты будут уничтожены или
приведены в стандартную форму.

В большинстве случаях, синтаксис выглядит так:

    <модификатор> [тип] [имя] {
        <параметр1>
        <параметр2>
        <параметр3>
        ...
        <параметрN>
    }

Контракты
---------

Контракты - это объект, который позволяет _отложить_ обработку
событие на другую супергруппу. Позволяет задать условие, в виде
сопоставления хранящегося типа, для активации переноса обработки.
И задать проброс данных из актуального события в отложенное.
Также требует обязательного позиционирования в супергруппе,
в противном случае откладывание не произойдёт.

Декларация контракта:

    contract [имя_контракта] [супергруппа] [имя_группы] [супергруппа]

Пример
------

    // Подавление обработчика BetterQuesting для 
    // предотвращения зачисление смерти в харкор режиме
    reverse mute betterquesting.handlers.EventHandler@onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent) {
      com.kentington.thaumichorizons.common.lib.EventHandlerEntity@onPlayerHurt(net.minecraftforge.event.entity.living.LivingHurtEvent)
    }

    // Группировка нескольких обработчиков в упорядоченную группу
    order group save_the_player_main {
      com.emoniph.witchery.common.GenericEvents@onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent)
      twilightforest.TFEventListener@entityHurts(net.minecraftforge.event.entity.living.LivingHurtEvent)
    }

    group save_the_player_extra {
      com.kentington.thaumichorizons.common.lib.EventHandlerEntity@onPlayerHurt(net.minecraftforge.event.entity.living.LivingHurtEvent)
    }
    
    supergroup net.minecraftforge.event.entity.living.LivingHurtEvent {
      // default_slot - определён не явно, HIGHEST
      // next_phase - тут HIGH, определён не явно
      // default_slot
      next_phase // далее для NORMAL
      // default_slot
      next_phase // далее для LOW
      save_the_player_main // Обработчики увидят фазу LOW
      save_the_player_extra
      // default_slot - все остальны обработчики LOW уйдут сюда,
      // определён системой не явно
      // next_phase
      // default_slot
      // Вы можете резместить здесь группу, в любом месте
    }

    // Объявляем контракт для группы save_the_player_main
    contract contract_save_the_player_main  openmods.entity.PlayerDamageEvent save_the_player_main  net.minecraftforge.event.entity.living.LivingHurtEvent
    contract contract_save_the_player_extra openmods.entity.PlayerDamageEvent save_the_player_extra net.minecraftforge.event.entity.living.LivingHurtEvent

    // Настраиваем условие перехода на сущность игрока
    condition contract_save_the_player_main  {
      entityLiving -> net.minecraft.entity.player.EntityPlayer
    }

    condition contract_save_the_player_extra {
      entityLiving -> net.minecraft.entity.player.EntityPlayer
    }

    // Настраиваем пробрас значение из сурогатного события
    mapping contract_save_the_player_extra {
      // Влияет на данные во всех контрактов от LivingHurtEvent
      amount -> ammount
    }

    group pre_save_the_player {
      openblocks.enchantments.LastStandEnchantmentsHandler@onHurt(openmods.entity.PlayerDamageEvent)
    }

    supergroup openmods.entity.PlayerDamageEvent {
      next_phase
      next_phase
      pre_save_the_player
      contract_save_the_player_main // вставляются контракты как обычные группы
      contract_save_the_player_extra
    }

Тут для группы save_the_player определяем явный
порядок срабатывание обработчиков "спасателей игрока":

1. Куклы из Witchery
2. Талисман жизни из Twilight Forest
3. Лечебный чан из Thaumic Horizons

P.s. Зачарование "Последний рубеж" (OpenBlocks), сюда не включён.
Он будет дальше.

Так же тут есть пример с mute, вместе с флагом reverse.

Я изменил код Thaumic Horizons так, чтобы при срабатывании лечебного
чана (исследование "Реинкарнация" в таумономиконе), я проходил
полный цикл смерти и возрождения (ложной). Что позволило применять
эффекты из Witchery "Липкие эффекты", "Липкие предметы" и талисман
инвентаря III из Twilight Forest. Но на хардкор режиме от
betterquesting (/bq_admin hardcore) столкнулся с уменьшением жизней.

По тому, когда срабатывает лечебный чан, обработчик смерти из
betterquesting, временно отправляется в бан.
reverse позволяет указывать обработчики, при выполнении
которых betterquesting не должен обрабатываться.

Если просто умру, жизни уменьшатся.

Про "Последний рубеж" (OpenBlocks):
В данном примере обработчик «Последнего рубежа» вынесен в
отдельную группу pre_save_the_player. Она работает через
событие PlayerDamageEvent, что не позволяет внедрить его
в последовательность LivingHurtEvent.

Потому мы заключаем контракт с save_the_player, что позволяет
перенести выполнение из LivingHurtEvent в PlayerDamageEvent.
А это в свою очередь открывает возможность задать конкретные
место в общей очереди.

В итоге получаем следующее:

1. Зачарование "Последний рубеж"
2. Куклы из Witchery
3. Талисман жизни из Twilight Forest
4. Лечебный чан из Thaumic Horizons

Функции
-------

У мода присутствует две функции:
1. Делать дамп, всех зарегистрированных обработчиков.
2. Runtime перезагрузка настроек.

Они доступны из меню клиента, в настройках мода.
Так же доступны в виде команд. (/deterministicevents [dump|reload])

В одиночном мире, хозяину мира, команды доступны
вне зависимости от режима читов. Для всех остальных, со
2-го уровня прав.

То есть, если возник баг из-за корявых приоритетов
или из-за вызова других обработчиков внутри другого.

То можно написать конфиг. И перезагрузить командой
/deterministicevents reload в консоли сервера/клиента.

И баг исправится, без полной перезагрузки всего сервера/клиента.

Планы
-----

Усовершенствование механизмов контрактов. В частности:
 - Условия откладывания
 - Обновления данных в идущих последовательно контрактов
