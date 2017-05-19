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



	//＜処理内容４-１＞支店別集計結果の出力
	/////////////////   outputSales method   //////////////
	public static boolean outputSales(HashMap<String,Long> salesTotalMap, HashMap<String,String> inforMap, String filePath){
		List<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String,Long>>(salesTotalMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>(){
			//@Override
			public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2){
				return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});
		FileWriter fw;
		BufferedWriter bw = null;
		try{
			File totalFile = new File(filePath);
			fw = new FileWriter(totalFile);
			bw = new BufferedWriter(fw);
			for(Entry<String,Long> s : entries){
				bw.write(s.getKey() + "," + inforMap.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		finally{
			if(bw != null){
				try{
					bw.close();
				}
				catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}

	public static boolean iutputData(String filepath, String eRule, String error, HashMap<String,String> inforMap, HashMap<String,Long> salesTotalMap){
		BufferedReader br = null;
		try{
			File file = new File(filepath);
			if(!file.exists()){
				System.out.println(error +"定義ファイルが存在しません");
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			String[] items = null;
			while((s = br.readLine()) != null){
				items = s.split(",");

				if((items.length != 2) || (!items[0].matches(eRule))){
					System.out.println(error +"定義ファイルのフォーマットが不正です");
					return false;
				}
				inforMap.put (items[0], items[1]);
				salesTotalMap.put (items[0], 0L);
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		finally{
			if(br != null){
				try{
					br.close();
				}
				catch (IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args){

		HashMap<String,String> branchLstmap = new HashMap<String,String>();
		HashMap<String,Long> branchSalesmap = new HashMap<String,Long>();
		HashMap<String,String> commodityLstmap = new HashMap<String,String>();
		HashMap<String,Long> commoditySalesmap = new HashMap<String,Long>();
		ArrayList<String> fileNames = new ArrayList<String>();
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}


		//例外によるエラー表示処理（予期せぬエラーが発生しました。）
		/*＜処理内容１＞支店定義ファイル読み込み*/
		if(!iutputData(args[0] + File.separator + "branch.lst", "^\\d{3}$", "支店", branchLstmap, branchSalesmap)){
			return;
		}
		if(!iutputData(args[0] + File.separator + "commodity.lst", "^\\w{8}$", "商品", commodityLstmap, commoditySalesmap)){
			return;
		}


		/*＜処理内容３-１、-２＞売上ファイルの読み込み-*/
		File dirName = new File(args[0]);
		//フォルダ内全ファイル名を配列として読み込み
		String[] filelist = dirName.list();

		//以下、全ファイル数繰り返す
		for(int i = 0; i < filelist.length; i++){
			String fileName = filelist[i];
			//該当データがファイル　かつ　名前が「8桁.rcd」
			if((new File(args[0], fileName).isFile()) && fileName.matches("^\\d{8}.rcd$")){
				fileNames.add(fileName);
			}
			//「filenames」リストに格納

		}
		Collections.sort(fileNames);
		for(int j = 0; j < (fileNames.size()-1); j++){
			int k = j + 1;
			int fileNameA = Integer.parseInt(fileNames.get(j).replaceAll("[^0-9]", ""));
			int fileNameB = Integer.parseInt(fileNames.get(k).replaceAll("[^0-9]", ""));

			if((fileNameB - fileNameA) != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		File sales;
		FileReader fr = null;
		BufferedReader br = null;
		try{
			for(int i = 0; i < (fileNames.size()); i++){
				sales = new File(args[0]+ File.separator + fileNames.get(i));
				fr = new FileReader(sales);
				br = new BufferedReader(fr);
				ArrayList<String> saler = new ArrayList<String>();
				String s;
				while((s = br.readLine()) != null){
					saler.add(s);
				}
				if(saler.size() != 3){
					System.out.println(fileNames.get(i) + "のフォーマットが不正です");
					return;
				}

				if(saler.get(2).matches(".*[^0-9].*")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				if(!branchLstmap.containsKey(saler.get(0))){
					System.out.println(fileNames.get(i) + "の支店コードが不正です");
					return;
				}

				if(!commodityLstmap.containsKey(saler.get(1))){
					System.out.println(fileNames.get(i) + "の商品コードが不正です");
					return;
				}
				/*＜処理内容３-１、-２＞売上ファイルの集計*/
				long soloSale = Long.parseLong(saler.get(2));

				long branchTally = branchSalesmap.get(saler.get(0));
				long branchTotal = branchTally + soloSale;

				if(branchTotal > 9999999999L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				long commodityTally = commoditySalesmap.get(saler.get(1));
				long commodityTotal = commodityTally + soloSale;

				if(commodityTotal > 9999999999L){
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
		}
		finally{
			if(br != null){
				try{
					br.close();
				}
				catch (IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		//＜処理内容４-１＞支店別集計結果の出力
		if(!outputSales(branchSalesmap, branchLstmap, args[0] + File.separator + "branch.out")){
			return;
		}
		if(!outputSales(commoditySalesmap, commodityLstmap, args[0] + File.separator + "commodity.out")){
			return;
		}
	}
}



