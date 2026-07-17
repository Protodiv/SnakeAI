package ua.snakeai.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named
import ua.snakeai.app.data.api.SnakeAiApi
import ua.snakeai.app.data.repository.SnakeAiRepositoryImp
import ua.snakeai.app.data.repository.SnakeAiRepository
import ua.snakeai.app.view.main.mainmenu.MainMenuViewModel
import ua.snakeai.app.view.main.playmenu.PlayMenuViewModel
import ua.snakeai.app.view.game.GameViewModel
import ua.snakeai.app.view.train.TrainDqnViewModel

val applicationModule = module {
    single { SnakeAiApi(get(), get(), get(named("ServerHost")), get(named("ServerPort"))) }
    single<SnakeAiRepository> { SnakeAiRepositoryImp(get()) }
    viewModel { MainMenuViewModel(get()) }
    viewModel { PlayMenuViewModel() }
    viewModel { GameViewModel(get()) }
    viewModel { TrainDqnViewModel(get()) }
}
