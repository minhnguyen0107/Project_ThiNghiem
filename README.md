# Đánh giá Thực nghiệm Ngưỡng Cắt (Cutoff) cho Hybrid MergeSort

Dự án này đo lường và phân tích hiệu năng của thuật toán **Hybrid MergeSort** (kết hợp giữa MergeSort đệ quy và Insertion Sort cho các mảng con kích thước nhỏ $\le \text{cutoff}$). Mục tiêu của thí nghiệm là tìm ra giá trị `cutoff` tối ưu trên kiến trúc CPU hiện đại thay vì sử dụng các hằng số kinh điển trong giáo trình (như `7` hoặc `10`).

---

## 1. Thiết lập Thí nghiệm

* **Kích thước dữ liệu ($n$):** $10,000$ ($10^4$), $100,000$ ($10^5$), và $1,000,000$ ($10^6$) phần tử.
* **Các phân phối dữ liệu đầu vào (`type`):**
  1. `random`: Dữ liệu phân phối ngẫu nhiên đều.
  2. `nearly_sorted`: Dữ liệu đã gần có thứ tự tăng dần.
  3. `reversed`: Dữ liệu sắp xếp theo thứ tự giảm dần hoàn toàn (trường hợp xấu nhất của Insertion Sort).
  4. `many_duplicates`: Dữ liệu có tỷ lệ trùng lặp giá trị cao.
* **Các chỉ số đo lường:**
  * `best_cutoff`: Giá trị ngưỡng cắt cho thời gian chạy trung bình thấp nhất.
  * `best_mean_ms`: Thời gian thực thi trung bình tại `best_cutoff` (tính bằng mili-giây).
  * `within_2pct`: Các giá trị `cutoff` có hiệu năng nằm trong ngưỡng sai lệch $\le 2\%$ so với `best_cutoff`.
  * `ci_overlap_with_best`: Tập hợp các giá trị `cutoff` có khoảng tin cậy (Confidence Interval) giao thoa với `best_cutoff` (không có sự khác biệt có ý nghĩa thống kê).
  * `speedup_vs_1`: Hệ số tăng tốc khi dùng `best_cutoff` so với MergeSort thuần túy (`cutoff = 1`).
  * `cutoff7_vs_best` / `cutoff10_vs_best`: Tỷ lệ thời gian chạy của mốc `cutoff = 7` và `cutoff = 10` so với mức tối ưu (`best_mean_ms`).

---

## 2. Kết quả Benchmark

| Loại dữ liệu (`type`) | Kích thước ($n$) | `best_cutoff` | Thời gian tối ưu (ms) | `cutoff = 1` (ms) | Hệ số tăng tốc (`speedup_vs_1`) | Chậm hơn tối ưu (`cutoff = 7`) | Chậm hơn tối ưu (`cutoff = 10`) | Các mốc trong ngưỡng 2% (`within_2pct`) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **many_duplicates** | 10,000 | **50** | 0.5125 | 0.8657 | **1.69x** | +42.33% | +19.09% | 50, 64 |
| **many_duplicates** | 100,000 | **50** | 6.0918 | 8.8861 | **1.46x** | +21.72% | +22.25% | 50 |
| **many_duplicates** | 1,000,000 | **32** | 95.6455 | 133.8966 | **1.40x** | +33.53% | +23.61% | 32 |
| **nearly_sorted** | 10,000 | **100** | 0.1444 | 0.3661 | **2.54x** | +55.04% | +33.06% | 100 |
| **nearly_sorted** | 100,000 | **100** | 6.7649 | 12.9122 | **1.91x** | +42.54% | +32.37% | 100 |
| **nearly_sorted** | 1,000,000 | **100** | 70.8979 | 112.9627 | **1.59x** | +26.47% | +21.78% | 100 |
| **random** | 10,000 | **50** | 0.7043 | 1.0462 | **1.49x** | +28.17% | +11.21% | 40, 50 |
| **random** | 100,000 | **26** | 9.2866 | 13.8056 | **1.49x** | +28.28% | +25.75% | 26 |
| **random** | 1,000,000 | **64** | 137.0164 | 187.3880 | **1.37x** | +17.51% | +9.87% | 32, 64, 100 |
| **reversed** | 10,000 | **12** | 0.1956 | 0.2851 | **1.46x** | +17.62% | +0.58% | 10, 12, 15 |
| **reversed** | 100,000 | **25** | 2.5618 | 3.7632 | **1.47x** | +16.13% | +16.67% | 25 |
| **reversed** | 1,000,000 | **24** | 25.2942 | 36.3216 | **1.44x** | +10.20% | +9.65% | 20, 21, 23, 24, 25 |

---

## 3. Phân tích Kỹ thuật & Nhận định

### 3.1. Hiệu quả của kỹ thuật Hybrid so với đệ quy thuần túy (`cutoff = 1`)
* Việc chuyển sang Insertion Sort ở các nhánh đệ quy cuối giúp loại bỏ phần lớn chi phí gọi hàm (function call overhead) và thao tác cấp phát/sao chép mảng phụ trợ.
* Hệ số tăng tốc trung bình đạt **1.61x** trên toàn bộ các kịch bản, cao nhất lên tới **2.54x** đối với mảng `nearly_sorted` kích thước $n = 10,000$.

### 3.2. Sự lỗi thời của các ngưỡng kinh điển (`cutoff = 7` và `10`)
* Các tài liệu kinh điển (như *Algorithms* của Robert Sedgewick) thường khuyến nghị `cutoff = 7`. Tuy nhiên, thực nghiệm cho thấy:
  * **`cutoff = 7`** chậm hơn mức tối ưu trung bình **28.3%** (tệ nhất là chậm hơn **55.04%** ở `nearly_sorted`, $n=10^4$).
  * **`cutoff = 10`** chậm hơn mức tối ưu trung bình **18.8%**.
* **Nguyên nhân phần cứng:** Kiến trúc CPU hiện đại sở hữu bộ nhớ đệm L1/L2 dung lượng lớn, cơ chế nạp trước bộ nhớ (hardware prefetcher) và bộ dự đoán nhánh (branch predictor) hiệu quả. Truy cập tuần tự trên vùng nhớ liên tục của Insertion Sort ở kích thước mảng con từ `24` đến `64` phần tử tận dụng tối đa cache line và nhanh hơn đáng kể so với chi phí chia tách đệ quy.

### 3.3. Ảnh hưởng của phân phối dữ liệu đầu vào
* **`nearly_sorted`:** Đạt hiệu năng đỉnh ở **`cutoff = 100`** trên mọi kích thước mảng. Do mảng đã gần có thứ tự, vòng lặp trong của Insertion Sort kết thúc ngay sau 1–2 lần so sánh, đưa độ phức tạp thực tế về tiệm cận $O(n)$.
* **`reversed`:** Đòi hỏi ngưỡng cắt nhỏ nhất (**`12` – `25`**). Vì mảng đảo ngược là trường hợp xấu nhất $O(n^2)$ của Insertion Sort, việc để `cutoff` quá lớn sẽ làm bùng nổ số phép dịch chuyển phần tử.
* **`random` và `many_duplicates`:** Điểm cân bằng tối ưu giữa chi phí đệ quy và chi phí dịch chuyển phần tử nằm ổn định trong dải **`26` – `64`**.

### 3.4. Tính ổn định thống kê (`ci_overlap_with_best`)
* Ở kích thước lớn ($n = 10^6$), ngoại trừ trường hợp `reversed` (dải giao thoa hẹp từ `18` đến `28`), khoảng tin cậy của các mức `cutoff` trải rộng từ `1` đến `100`. Điều này phản ánh rằng với dữ liệu lớn, thời gian thực thi chịu chi phối nhiều hơn bởi băng thông bộ nhớ (memory bandwidth) ở các tầng trộn (merge) trên cùng và độ nhiễu hệ thống.

---

## 4. Khuyến nghị Lựa chọn `cutoff` Mặc định

Dựa trên sự giao thoa khoảng tin cậy (`ci_overlap_with_best`) và hiệu năng thực tế trên cả 4 loại phân phối dữ liệu:

* **Khuyến nghị tổng quát (An toàn nhất):** **`cutoff = 24` đến `28`** (tiêu biểu là **`25`** hoặc **`28`**). Giá trị này nằm trong vùng giao thoa khoảng tin cậy với điểm tối ưu ở **11/12** kịch bản thử nghiệm, đảm bảo hiệu năng cao trên dữ liệu ngẫu nhiên/trùng lặp mà không bị phạt hiệu năng nặng ở trường hợp xấu nhất (`reversed`).
* **Khuyến nghị cho dữ liệu thực tế có tính chất gần sắp xếp:** **`cutoff = 32` đến `64`**. Phù hợp làm cấu hình mặc định trong các thư viện chuẩn (tương đồng với `MIN_RUN = 32` trong thuật toán TimSort).