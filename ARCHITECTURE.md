# LyricMemo アプリケーション構成ガイド

このドキュメントは、LyricMemoアプリの全体的なアーキテクチャと、各パッケージや主要クラスの役割について説明します。

---

## 1. 全体アーキテクチャ

このアプリは、Googleが推奨するモダンなAndroidアプリ開発の標準的なアーキテクチャを採用しています。

- **UI層 (View)**: `Fragment`と`XML`で画面を構成。ユーザーの操作を受け付け、ViewModelからのデータ変更を画面に反映します。
- **ViewModel層**: UIの状態(UI State)を保持し、ビジネスロジックを実行します。UIからのイベントを処理し、Repositoryにデータ操作を依頼します。
- **データ層 (Model)**: `Repository`パターンを採用。アプリに必要なデータの取得・保存・管理を行います。UI層やViewModelは、データがどこから来るのか（APIなのかDBなのか）を意識する必要がありません。

![MVVMアーキテクチャ](https://developer.android.com/static/topic/libraries/architecture/images/mad-arch-overview.png)
*（Google推奨のアプリ アーキテクチャ図）*

---

## 2. パッケージ構成と各クラスの役割

### `data` パッケージ (データ層)
アプリのデータに関するすべてのロジックが集約されています。

- **`api/VocaDbApi.kt`**: 
    - Retrofitライブラリのインターフェース。
    - VocaDBサーバーと通信するためのAPIエンドポイントを定義しています。

- **`db/`**: 
    - `AppDatabase.kt`: Roomデータベース本体を定義します。
    - `SavedSong.kt`: 保存する曲データのテーブル定義（Entity）です。
    - `SavedSongDao.kt`: データベースへの読み書き（CRUD）操作を行うメソッドを定義しています。

- **`model/`**: 
    - `VocaDbModels.kt`: VocaDB APIからのレスポンスデータ（曲、アーティスト情報など）を格納するデータクラス（DTO）を定義しています。

- **`repository/`**: 
    - `VocaDbRepository.kt`: VocaDB APIとの通信を実際に行うクラス。役割ごとにメソッドが分割されています（曲名検索、アーティスト検索など）。
    - `SavedSongRepository.kt`: ローカルのRoomデータベースへの保存・削除・読み込みを行うクラス。

### `di` パッケージ (依存性の注入)

- **`AppModule.kt`**: 
    - Hiltを使って、アプリ全体で必要となるインスタンス（`Retrofit`, `AppDatabase`など）の生成方法を定義しています。

### `ui` パッケージ (UI層)

画面表示とユーザー操作の受付を担当します。

- **`main/MainActivity.kt`**: 
    - アプリの単一のActivityであり、すべてのFragmentをホストする土台です。

- **`home/HomeFragment.kt`**: 
    - アプリ起動時のホーム画面。「検索」と「リスト」への分岐ボタンを表示します。

- **`search/`**: 
    - `SearchFragment.kt`: 曲名またはアーティスト名を検索する画面。検索タイプに応じて、曲リストまたはアーティストリストを表示します。
    - `SongSearchViewModel.kt`: `activityViewModels()`で共有されるViewModel。検索画面の状態管理（クエリ、検索タイプ、結果のリストなど）を担当します。
    - `SearchResultAdapter.kt`, `ArtistAdapter.kt`: 検索結果を`RecyclerView`に表示するためのアダプターです。

- **`list/` (SavedSongListFragment, SongListFragment)**: 
    - `SavedSongListFragment.kt`: DBに保存した曲の一覧を表示する画面。ソートや削除、手動追加（FAB）、ツールバーでの検索機能を持ちます。
    - `SavedSongListViewModel.kt`: 保存済み曲リストの取得、検索、ソート、削除などのロジックを担当します。
    - `SongListFragment.kt`: `SearchFragment`でアーティストを選択した後に表示される、そのアーティストの曲一覧画面です。
    - `SongListViewModel.kt`: `SongListFragment`専用のViewModel。**ページネーション（無限スクロール）**を実装し、アーティストの曲を順次読み込みます。

- **`details/`**: 
    - `LyricsDetailFragment.kt`: 歌詞の詳細を表示する画面。サムネイル、YouTube再生、保存機能もここにあります。
    - `LyricsDetailViewModel.kt`: 保存処理など、この画面固有のロジックを担当します。

- **`input/`**: 
    - `InputLyricsFragment.kt`: 歌詞を手動で入力して保存するための画面です。
    - `InputLyricsViewModel.kt`: 入力されたデータのバリデーションとDBへの保存ロジックを担当します。

---

## 3. 主要な処理フロー（検索〜歌詞表示）

ユーザーが曲を検索してから歌詞を表示するまでの、代表的な処理の流れを解説します。

**1. 検索 (`SearchFragment`)**
   - `EditText`に入力されたテキストが`SongSearchViewModel.onQueryChanged()`に通知されます。
   - `SongSearchViewModel`は`debounce`（間引き）の後、`searchArtists()`または`searchSongsByName()`を呼び出します。
   - `VocaDbRepository`が`VocaDbApi`を使ってAPIを呼び出し、アーティストまたは曲のリストを取得します。
   - 取得したリストは`artistListState`または`songListState`を通じて`SearchFragment`に通知され、`RecyclerView`に表示されます。

**2. 曲一覧表示 (`SongListFragment`)**
   - `SearchFragment`でアーティストが選択されると、`findNavController().navigate()`でアーティストIDと名前が`SongListFragment`に渡されます。
   - `SongListViewModel`は`SavedStateHandle`でIDを受け取り、`repository.searchSongsByArtistId()`を呼び出して曲の最初のページを取得します。
   - `SongListFragment`は`RecyclerView`のスクロールを監視し、下端に達すると`viewModel.loadMoreSongs()`を呼び出して次のページを読み込みます。

**3. 歌詞表示 (`LyricsDetailFragment`)**
   - 曲リストで曲が選択されると、`sharedViewModel.selectSong()`が呼び出され、選択された`SongItem`が`lyricsUiState`にセットされます。
   - `findNavController().navigate()`で`LyricsDetailFragment`に遷移します。
   - `LyricsDetailFragment`は`activityViewModels()`で共有ViewModel（`SongSearchViewModel`）を取得し、`lyricsUiState`を監視して曲名、アーティスト名、歌詞などをUIに表示します。

**4. 保存 (`LyricsDetailFragment`)**
   - 保存ボタン(FAB)が押されると、`LyricsDetailViewModel.saveSong()`が呼び出されます。
   - このとき、`searchViewModel.lyricsUiState.value`から現在の表示データを取得して引数に渡します。
   - `LyricsDetailViewModel`が`[DOCUMENT.md](DOCUMENT.md)SavedSongRepository`を通じて、`SavedSongDao`を呼び出し、DBにデータを`INSERT`します。
