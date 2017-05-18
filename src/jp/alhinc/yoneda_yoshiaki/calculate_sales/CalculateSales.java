package jp.alhinc.yoneda_yoshiaki.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {
	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		HashMap<String,String> branchLstmap = new HashMap<String,String>();
		HashMap<String,Long> branchSalesmap = new HashMap<String,Long>();
		HashMap<String,String> commodityLstmap = new HashMap<String,String>();
		HashMap<String,Long> commoditySalesmap = new HashMap<String,Long>();
		ArrayList<String> filenames = new ArrayList<String>();

		//例外によるエラー表示処理（予期せぬエラーが発生しました。）
		/*＜処理内容１＞支店定義ファイル読み込み*/

		BufferedReader br = null;
		try{
			File fileb = new File(args[0]+ File.separator +"branch.lst");
			if(!fileb.exists()){
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(fileb);
			br = new BufferedReader (fr);
			String s;
			String[] items = null;
			while((s = br.readLine()) != null){
				items = s.split(",");

				if((items.length != 2) || (!items[0].matches("^\\d{3}$"))){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branchLstmap.put (items[0],items[1]);
				branchSalesmap.put (items[0],0L);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			if(br != null){
				try{
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		/*＜処理内容２＞商品定義ファイルの読み込み*/
		BufferedReader br2 = null;
		try{
			File filec = new File(args[0]+ File.separator +"commodity.lst");
			if(!filec.exists()){
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(filec);
			br2 = new BufferedReader (fr);
			String s;
			String[] items = null;
			while((s = br2.readLine()) != null){
				items = s.split(",");

				if((items.length != 2)  || (!items[0].matches("^\\w{8}$"))){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commodityLstmap.put (items[0],items[1]);
				commoditySalesmap.put (items[0],0L);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			if(br2 != null){
				try{
					br2.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}


		/*＜処理内容３-１、-２＞売上ファイルの読み込み-*/
		File dirName = new File (args[0]);
		//フォルダ内全ファイル名を配列として読み込み
		String[] filelist = dirName.list();

		//以下、全ファイル数繰り返す
		for(int i = 0; i < filelist.length; i++){
			String faileName =filelist[i];

			//名前が「8桁.rcd」
			if(faileName.matches("^\\d{8}.rcd$")){
				//「filenames」リストに格納
				filenames.add(faileName);
			}
		}

		Collections.sort(filenames);
		for(int j = 0; j < (filenames.size()-1); j++){
			int k = j + 1;
			int fileName1 = Integer.parseInt(filenames.get(j).replaceAll("[^0-9]",""));
			int fileName2 = Integer.parseInt(filenames.get(k).replaceAll("[^0-9]",""));

			if((fileName2 - fileName1) != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		File sales;
		FileReader fr = null;
		BufferedReader br3 = null;
		try{
			for(int i = 0; i < (filenames.size()); i++){
				sales = new File(args[0]+ File.separator +filenames.get(i));
				fr = new FileReader (sales);
				br3 = new BufferedReader (fr);
				ArrayList<String> saler = new ArrayList<String>();
				String s;
				while((s = br3.readLine()) != null){
					saler.add(s);
				}
				if(saler.size() != 3){
					System.out.println(filenames.get(i) + "のフォーマットが不正です");
					return;
				}

				if(saler.get(2).matches(".*[^0-9].*")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				if(!branchLstmap.containsKey(saler.get(0))){
					System.out.println(filenames.get(i) + "の支店コードが不正です");
					return;
				}

				if(!commodityLstmap.containsKey(saler.get(1))){
					System.out.println(filenames.get(i) + "の商品コードが不正です");
					return;
				}
				/*＜処理内容３-１、-２＞売上ファイルの集計*/
				long solosale = Long.parseLong(saler.get(2));

				long branchTally = branchSalesmap.get(saler.get(0));
				long branchTotal = branchTally + solosale;

				if( branchTotal > 9999999999L ){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				long commodityTally = commoditySalesmap.get(saler.get(1));
				long commodityTotal = commodityTally + solosale;

				if( commodityTotal > 9999999999L ){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchSalesmap.put(saler.get(0), branchTotal);
				commoditySalesmap.put(saler.get(1), commodityTotal);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			if(br3 != null){
				try{
					br3.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
				}
			}
		}




		//＜処理内容４-１＞支店別集計結果の出力
		FileWriter fw;
		BufferedWriter bw = null;

		List<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String,Long>>(branchSalesmap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {
			//@Override
			public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});


		try{
			File brFile = new File(args[0] + File.separator + "branch.out");
			fw = new FileWriter (brFile);
			bw = new BufferedWriter (fw);

			for (Entry<String,Long> s : entries) {
				bw.write(s.getKey() + "," + branchLstmap.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			if(bw != null){
				try{
					bw.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
				}
			}
		}


		FileWriter fw2;
		BufferedWriter bw2 = null;

		List<Map.Entry<String,Long>> entries2 = new ArrayList<Map.Entry<String,Long>>(commoditySalesmap.entrySet());
		Collections.sort(entries2, new Comparator<Map.Entry<String,Long>>() {
			//@Override
			public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		try{
			File coFile = new File(args[0] + File.separator + "commodity.out");
			fw2 = new FileWriter(coFile);
			bw2 = new BufferedWriter (fw2);

			for (Entry<String,Long> s : entries2) {
				bw2.write(s.getKey() + "," + commodityLstmap.get(s.getKey()) + "," + s.getValue());
				bw2.newLine();
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			if(bw2 != null){
				try{
					bw2.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
				}
			}
		}
	}
}


