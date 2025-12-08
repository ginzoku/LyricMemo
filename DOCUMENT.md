# このアプリで使われている主要技術

このドキュメントは、LyricMemoアプリで採用されている主要なAndroidライブラリと設計パターンについて解説します。

---

## 1. Hilt (依存性の注入)

### 役割

クラス間で必要となるインスタンス（オブジェクト）の生成と管理を自動化するライブラリです。クラス自身が依存するオブジェクトを生成するのではなく、外部（Hilt）から与えてもらう（注入してもらう）ことで、クラス間の結合度を下げ、テストや修正を容易にします。

### このアプリでの使用例

- **`di/AppModule.kt`**: `Retrofit`や`AppDatabase`など、Hiltが直接生成できないクラスのインスタンス作成方法を定義しています（`@Module`と`@Provides`）。
- **`data/repository/VocaDbRepository.kt`**: `@Inject constructor(...)` を使って、APIクライアント(`VocaDbApi`)をHiltから注入してもらっています。
- **`ui/search/SongSearchViewModel.kt`**: `@HiltViewModel`を使い、ViewModelのコンストラクタに必要な`VocaDbRepository`や`SavedSongRepository`を注入しています。
- **`ui/search/SearchFragment.kt`など**: `@AndroidEntryPoint`アノテーションを付けることで、`by viewModels()`や`by activityViewModels()`を使ってHiltが管理するViewModelインスタンスを受け取っています。

---

## 2. Coroutine (非同期処理)

### 役割

ネットワーク通信やデータベースアクセスなど、時間のかかる処理（重い処理）をメインスレッド（UIスレッド）をブロックせずに行うための仕組みです。これにより、アプリがフリーズすることなく、スムーズなユーザー体験を提供できます。

### このアプリでの使用例

- **`ui/search/SongSearchViewModel.kt`**: `viewModelScope.launch { ... }` を使って、Coroutineを起動し、その中でVocaDb APIの呼び出しを行っています。`viewModelScope`はViewModelのライフサイクルと連動しており、ViewModelが破棄されると自動的にCoroutineもキャンセルされます。
- **`data/repository/VocaDbRepository.kt`**: `suspend fun` キーワードが付いたメソッドは「中断可能な関数」であり、Coroutineの中から呼び出されることを前提としています。API通信などの完了を待つ間、スレッドを無駄に占有しません。
- **`Flow`**: リアルタイム検索機能やDBの監視で利用しています。`debounce`（間引き）や`combine`（組み合わせ）などの演算子を使って、ユーザーの入力やデータの変更を効率的に処理し、UIを自動更新する基盤となっています。

---

## 3. ViewModel (MVVMアーキテクチャ)

### 役割

MVVM (Model-View-ViewModel) アーキテクチャの中核をなすコンポーネントです。UI（View）に関連するデータを保持し、ビジネスロジックを実行します。画面回転などの構成変更が起きてもデータを保持し続けるため、UIの状態管理に適しています。

### このアプリでの使用例

- **`ui/search/SongSearchViewModel.kt`**: 
    - 検索結果のリストや、歌詞詳細のデータ（`LyricsUiState`）を`StateFlow`として保持しています。
    - `searchSongs()`や`selectSong()`などのメソッドで、Repositoryからデータを取得・加工し、UIの状態を更新します。
- **`ui/search/SearchFragment.kt`**: 
    - `by activityViewModels()`でViewModelのインスタンスを取得します。
    - `viewModel.uiState.collect { ... }` のように`StateFlow`を監視（購読）し、データが更新されるたびにUI（RecyclerViewやTextViewなど）を自動的に更新します。

### データ更新の流れ (リアクティブUI)

このアプリでは、`Flow`と`ViewModel`を組み合わせることで、**リアクティブ（反応型）**なUI更新を実現しています。`SavedSongListViewModel`を例に説明します。

1. **データソースの定義**: ViewModelは2つの情報源（`Flow`）を監視します。
    - `savedSongRepository.getAllSongs()`: DB内の曲リスト。DBが変更されると自動的に新しいリストを放出します。
    - `_sortOrder`: ユーザーが選択した並び順。

2. **`combine`による合成**: `combine`演算子は、これらの`Flow`を監視し、どちらかが変化するたびに、2つの最新の値を使って新しいソート済みのリストを生成します。

3. **`StateFlow`への変換**: `stateIn`演算子によって、`combine`で生成されたリストが`StateFlow`に変換されます。`StateFlow`は常に最新の状態を保持し、UIに公開されます。

4. **UIの監視と更新**: Fragment側では、`viewModel.savedSongs.collect`でこの`StateFlow`を監視します。`StateFlow`の値が（DBの変更やソート順の変更によって）更新されると、`collect`ブロックが実行され、`Adapter`に新しいリストを渡して画面が自動的に再描画されます。

この仕組みにより、「データが変われば、UIが勝手に更新される」という宣言的なプログラミングが可能になり、UIの状態管理がシンプルになります。

---

## 4. Navigation Component (画面遷移)

### 役割

Fragment間の遷移を簡単かつ視覚的に管理するためのライブラリです。遷移のアニメーション、画面間のデータ受け渡し、ディープリンクなどを統一的に扱うことができます。

### このアプリでの使用例

- **`res/navigation/nav_graph.xml`**: 
    - アプリ内のすべての画面（Fragment）と、それらの間の遷移（Action）を定義した「設計図」です。
    - `app:startDestination="@id/homeFragment"` という属性で、アプリ起動時に最初に表示する画面を指定しています。

- **`ui/main/activity_main.xml`**: 
    - `MainActivity`のレイアウトファイルです。
    - ここに配置された`NavHostFragment`が`app:navGraph="@navigation/nav_graph"`という属性を持ち、上記の「設計図」を読み込んで画面遷移を管理します。

- **Fragmentからの画面遷移**: 
    - `ui/home/HomeFragment.kt`などで`findNavController().navigate(R.id.action_homeFragment_to_searchFragment)`のように、`nav_graph.xml`で定義したIDを使って安全に画面遷移を実行しています。

### アプリ起動時の画面表示フロー

1. アプリが起動すると`MainActivity`が作られます。
2. `MainActivity`が自身のレイアウト(`activity_main.xml`)を読み込みます。
3. レイアウト内の`NavHostFragment`が、指定された`nav_graph.xml`を読み込みます。
4. `NavHostFragment`は、`nav_graph.xml`の`startDestination`に指定されている`homeFragment`を見つけます。
5. `NavHostFragment`が自身のコンテナ内に`HomeFragment`のインスタンスを生成して表示します。

---

## 5. Room (データベース)

### 役割

SQLiteデータベースを簡単に扱うための、Google公式のライブラリ（ORM）です。ボイラープレートコード（お決まりの冗長なコード）を減らし、コンパイル時にSQLクエリを検証してくれるため、安全かつ直感的にデータベース操作を行えます。

### このアプリでの使用例

- **`data/db/SavedSong.kt`**: `@Entity`アノテーションを使い、`saved_songs`というテーブルの構造を定義しています。
- **`data/db/SavedSongDao.kt`**: `@Dao`アノテーションが付いたインターフェースです。`@Insert`, `@Delete`, `@Query`などのアノテーションを使って、SQL文をメソッドとして定義しています。
- **`data/db/AppDatabase.kt`**: `@Database`アノテーションで、アプリ全体のデータベースを定義し、どのEntityとDAOを含むかを指定しています。

---

## 6. Glide (画像読み込み)

### 役割

インターネット上のURLから画像を非同期で読み込み、`ImageView`に表示するためのライブラリです。画像のキャッシュ（一時保存）、リサイズ、メモリ管理などを自動で行い、高速で効率的な画像表示を実現します。

### このアプリでの使用例

- **`ui/details/LyricsDetailFragment.kt`**: `Glide.with(this).load(state.thumbnailUrl).into(ivThumbnail)` のように記述することで、VocaDB APIから取得したサムネイル画像のURLを元に、画像をダウンロードして`ImageView`に表示しています。
    - `.placeholder()`: 画像の読み込み中に表示しておく仮の画像を指定します。
    - `.error()`: 画像の読み込みに失敗した場合に表示する画像を指定します。
