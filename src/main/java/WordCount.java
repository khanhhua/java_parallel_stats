import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordCount {
	private List<String> filePathList;
	private final ExecutorService pool;
	private volatile int result;
	
	public WordCount(){
		filePathList = new ArrayList<String>();
		
		pool = Executors.newCachedThreadPool();
	}
	
	public void queue(String filePath) {
		System.out.println("Queuing: " + filePath);
		filePathList.add(filePath);
	}
	
	public void count(final Callback callback) {
		for(String item:filePathList) {
			Path path = FileSystems.getDefault().getPath(item);
			final String absPath = path.normalize().toAbsolutePath().toString();
			
			pool.execute(new Runnable(){
				public void run() {
					System.out.println("Processing: " + absPath);
					File file = new File(absPath);
					long size = file.length();
					
					System.out.println("==> " + absPath + " done: " + size);
					
					result += size;					
				}
			});
		}
		System.out.println("synchronized (syncLock)");
		
		pool.shutdown();
		try {
			pool.awaitTermination(30, TimeUnit.SECONDS);
			callback.call(null, result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public interface Callback {
		public void call(Object error, Object result);
	}
	
	public static void main(String[] args) {
		File currentDirectory = new File(new File(".").getAbsolutePath());
		System.out.println("CWD: " + currentDirectory.getAbsolutePath());
		
		WordCount wc = new WordCount();
		
		if (args.length > 0) {
			for(String path:args) {
				wc.queue(path);
			}
			wc.count(new Callback() {
				public void call(Object error, Object result) {
					System.out.println("Total: " + result.toString());
				}
			});
		} else {
			System.out.println("java WordCount file_path_a file_path_b");
		}
	}
}