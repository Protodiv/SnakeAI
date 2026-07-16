package ua.snakeai.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.snakeai.app.data.api.SnakeAiApi
import ua.snakeai.app.data.repository.SnakeAiRepositoryImp
import ua.snakeai.app.data.repository.SnakeAiRepository
import ua.snakeai.app.view.main.mainmenu.MainMenuViewModel
import ua.snakeai.app.view.main.playmenu.PlayMenuViewModel
import ua.snakeai.app.view.game.GameViewModel

val applicationModule = module {
    single { SnakeAiApi(get()) }
    single<SnakeAiRepository> { SnakeAiRepositoryImp(get()) }
    viewModel { MainMenuViewModel(get()) }
    viewModel { PlayMenuViewModel() }
    viewModel { GameViewModel(get()) }
}
