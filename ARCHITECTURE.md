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
    - VocaDBサーバーと通信するためのAPIエンドポイント（`@GET("api/songs")`など）を定義しています。

- **`db/`**: 
    - `AppDatabase.kt`: Roomデータベース本体を定義します。
    - `SavedSong.kt`: 保存する曲データのテーブル定義（Entity）です。
    - `SavedSongDao.kt`: データベースへの読み書き（CRUD）操作を行うメソッドを定義しています。

- **`model/`**: 
    - `VocaDbModels.kt`: VocaDB APIからのレスポンスデータ（曲情報、PV情報など）を格納するためのデータクラス（DTO）を定義しています。

- **`repository/`**: 
    - `VocaDbRepository.kt`: VocaDB APIとの通信を実際に行うクラス。APIから取得したデータを加工して返します。
    - `SavedSongRepository.kt`: ローカルのRoomデータベースへの保存・削除・読み込みを行うクラス。

### `di` パッケージ (依存性の注入)

- **`AppModule.kt`**: 
    - Hiltを使って、アプリ全体で必要となるインスタンス（`Retrofit`, `AppDatabase`など）の生成方法を定義しています。
    - アプリ内の様々な場所でこれらのインスタンスを再利用（シングルトン化）し、効率的なリソース管理を実現します。

### `ui` パッケージ (UI層)

画面表示とユーザー操作の受付を担当します。

- **`main/MainActivity.kt`**: 
    - アプリの単一のActivityであり、すべてのFragmentをホストする土台です。
    - Navigation Componentの`NavHostFragment`を保持しています。

- **`home/HomeFragment.kt`**: 
    - アプリ起動時のホーム画面。「検索」と「リスト」への分岐ボタンを表示します。

- **`search/`**: 
    - `SearchFragment.kt`: 曲名やアーティスト名で検索を行う画面です。リアルタイム検索機能と検索結果のリスト表示を担当します。
    - `SongSearchViewModel.kt`: `activityViewModels()`で共有されるViewModel。検索クエリ、検索結果、詳細画面に表示するデータなど、検索フロー全体の状態を管理します。
    - `SearchResultAdapter.kt`: 検索結果を`RecyclerView`に表示するためのアダプターです。

- **`details/`**: 
    - `LyricsDetailFragment.kt`: 歌詞の詳細を表示する画面。サムネイル、YouTube再生ボタン、保存ボタン（FAB）もここにあります。
    - `LyricsDetailViewModel.kt`: `viewModels()`でこの画面専用に作られるViewModel。保存処理など、この画面固有のロジックを担当します。

- **`list/`**: 
    - `SavedSongListFragment.kt`: DBに保存した曲の一覧を表示する画面。ソート機能や長押しでの削除、新規追加FABを持ちます。
    - `SavedSongListViewModel.kt`: 保存済み曲リストの取得やソート、削除などのロジックを担当します。
    - `SavedSongAdapter.kt`: 保存済み曲を`RecyclerView`に表示するためのアダプターです。

- **`input/`**: 
    - `InputLyricsFragment.kt`: 歌詞を手動で入力して保存するための画面です。
    - `InputLyricsViewModel.kt`: 入力されたデータのバリデーション（必須チェック）とDBへの保存ロジックを担当します。
