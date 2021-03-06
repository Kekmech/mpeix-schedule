package com.kekmech.schedule.di

import com.google.gson.Gson
import com.kekmech.schedule.di.factories.GsonFactory
import com.kekmech.schedule.di.factories.HttpClientFactory
import com.kekmech.schedule.dto.ScheduleType
import com.kekmech.schedule.helpers.ModuleProvider
import com.kekmech.schedule.repository.ScheduleRepository
import com.kekmech.schedule.repository.sources.*
import com.kekmech.schedule.repository.sources.search.DatabaseSearchResultsSource
import com.kekmech.schedule.repository.sources.search.MergedSearchResultsSource
import com.kekmech.schedule.repository.sources.search.MpeiSearchResultsSource
import io.ktor.client.*
import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.Slf4JLoggerFactory
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import java.util.*

private val DATA_TYPE_GROUP = ScheduleType.GROUP.raw
private val DATA_TYPE_PERSON = ScheduleType.PERSON.raw

private val GROUP_ID_QUALIFIER = named("${DATA_TYPE_GROUP}_id")
private val GROUP_SCHEDULE_QUALIFIER = named("${DATA_TYPE_GROUP}_schedule")
private val GROUP_SESSION_QUALIFIER = named("${DATA_TYPE_GROUP}_session")

private val PERSON_ID_QUALIFIER = named("${DATA_TYPE_PERSON}_id")
private val PERSON_SCHEDULE_QUALIFIER = named("${DATA_TYPE_PERSON}_schedule")
private val PERSON_SESSION_QUALIFIER = named("${DATA_TYPE_PERSON}_session")

class AppModule : ModuleProvider({
    single { GsonFactory.create() } bind Gson::class
    single { HttpClientFactory.create() } bind HttpClient::class
    single { Slf4JLoggerFactory.getInstance("SCHEDULE") } bind InternalLogger::class
    single { Locale.GERMAN } bind Locale::class


    single(GROUP_ID_QUALIFIER) { IdSource(get(), get(), DATA_TYPE_GROUP) }
    single(GROUP_SCHEDULE_QUALIFIER) { ScheduleSource(get(), get(), get(), get(), get(GROUP_ID_QUALIFIER), DATA_TYPE_GROUP) }
    single(GROUP_SESSION_QUALIFIER) { SessionSource(get(GROUP_SCHEDULE_QUALIFIER), get(), get()) }


    single(PERSON_ID_QUALIFIER) { IdSource(get(), get(), DATA_TYPE_PERSON) }
    single(PERSON_SCHEDULE_QUALIFIER) { ScheduleSource(get(), get(), get(), get(), get(PERSON_ID_QUALIFIER), DATA_TYPE_PERSON) }
    single(PERSON_SESSION_QUALIFIER) { SessionSource(get(PERSON_SCHEDULE_QUALIFIER), get(), get()) }


    single { DatabaseSearchResultsSource(get()) }
    single { MpeiSearchResultsSource(get(), get(), get()) }
    single {
        ScheduleRepository(
            groupIdSource = get(GROUP_ID_QUALIFIER),
            groupScheduleSource = get(GROUP_SCHEDULE_QUALIFIER),
            personScheduleSource = get(PERSON_SCHEDULE_QUALIFIER),
            groupSessionSource = get(GROUP_SESSION_QUALIFIER),
            personSessionSource = get(PERSON_SESSION_QUALIFIER),
            mergedSearchSource = MergedSearchResultsSource(get(), get(), get())
        )
    } bind ScheduleRepository::class
})

object Logger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) = println(message)
}
