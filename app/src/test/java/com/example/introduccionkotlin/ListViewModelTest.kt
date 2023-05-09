package com.example.introduccionkotlin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.repository.CountriesRepository
import com.example.introduccionkotlin.ui.home.ListViewModel
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class ListViewModelTest {
    @get:Rule
    var rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: CountriesRepository

    @InjectMocks
    private lateinit var listViewModel : ListViewModel

    private var testSingle : Single<List<Country>>? = null

    @Before
    fun setUp (){
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun getCountriesSuccess() {
        val country = Country("countryName", "capital", "url")
        val countriesList: ArrayList<Country> = arrayListOf(country)
        testSingle = Single.just(countriesList)
       // `when`(repository.getCountries()).thenReturn(testSingle)
        listViewModel = ListViewModel(repository)

        listViewModel.refresh()
        Assert.assertEquals(1, listViewModel.countries.size)
        Assert.assertEquals(false, listViewModel.error)
    }

    @Test
    fun getCountriesFail() {
        testSingle = Single.error(Throwable())
      //  `when`(repository.getCountries()).thenReturn(testSingle)
        listViewModel = ListViewModel(repository)

        listViewModel.refresh()
        Assert.assertEquals(true, listViewModel.error)
    }

    @Before
    fun setUpRxSchedulers () {
        val immediate = object : Scheduler() {
            override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
                return super.scheduleDirect(run, 0, unit)
            }
            override fun createWorker() : Worker {
                return ExecutorScheduler.ExecutorWorker(Executor { it.run() }, false)
            }
        }
        RxJavaPlugins.setInitIoSchedulerHandler { scheduler -> immediate }
        RxJavaPlugins.setInitComputationSchedulerHandler { scheduler -> immediate }
        RxJavaPlugins.setInitNewThreadSchedulerHandler { scheduler -> immediate }
        RxJavaPlugins.setInitSingleSchedulerHandler { scheduler -> immediate }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler -> immediate }
    }
}