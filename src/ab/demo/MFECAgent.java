/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.VisionUtils;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class MFECAgent implements Runnable {
	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	public static HashMap<Double, HashMap<Integer, Integer>> ADic_Ag;
	public static HashMap<Integer,int[][]> hashToarrayStateDic;
	public int numTimestamp = 6001;
	private int prevScore;
	public boolean useMFEC = true; 
	
	public double gamma = 1;
	public ArrayList<Integer> rewardHist = new ArrayList<Integer>();
	public ArrayList<Integer> stateHist = new ArrayList<Integer>();
	public ArrayList<Double> actionHist = new ArrayList<Double>();
	
	public ArrayList<Integer> totalRewardHist = new ArrayList<Integer>();
	public boolean trainingFlag = false;
	public boolean newGame = true;
	public int maxScore = Integer.MIN_VALUE;

	// a standalone implementation of the Naive Agent
	public MFECAgent() {
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		firstShot = true;
		randomGenerator = new Random();
 		
 		ADic_Ag = new HashMap<Double, HashMap<Integer, Integer>>();
 		hashToarrayStateDic = new HashMap<Integer,int[][]>();
		for(double x = 0.0; x<80.0 ; x = x + 0.1){
			double action = Math.round(x*10.0)/10.0;
			ADic_Ag.put(action, new HashMap<Integer,Integer>()); 
		}
		
		// Read previous dic
		try{
		    FileInputStream fis = new FileInputStream("AdicMap_6000.data");
		    ObjectInputStream ois = new ObjectInputStream(fis);
		    ADic_Ag = (HashMap<Double, HashMap<Integer, Integer>>) ois.readObject();
		    ois.close();
		    fis.close();
		
		    FileInputStream fis2 = new FileInputStream("str2arrayDic_6000.data");
		    ObjectInputStream ois2 = new ObjectInputStream(fis2);
		    hashToarrayStateDic = (HashMap<Integer,int[][]>) ois2.readObject();
		    ois2.close();
		    fis2.close();
		 }catch(IOException ioe)
		 {
		    ioe.printStackTrace();
		 }catch(ClassNotFoundException c)
		 {
		    System.out.println("File not found");
		    c.printStackTrace();
		 }
		 ActionRobot.GoFromMainMenuToLevelSelection();
	}

	public void learn(){
		System.out.println("####### End of Level. Let's learn! #######");
		/* ############## Start Learning ##############*/
		for(int t = rewardHist.size(); t>0;t--){
			double action = actionHist.get(t-1);
			int statestr = stateHist.get(t-1);
			int cummReward = 0;
			for(int i = t; i>0;i--) cummReward += rewardHist.get(i-1)*Math.pow(gamma, t-i);
			
			System.out.print("At timestep "+t+", for state: " + statestr + ", action: "+action+ ", ");
			if(ADic_Ag.get(action).containsKey(statestr)){
				int prevReward = ADic_Ag.get(action).get(statestr);
				if(prevReward < cummReward){
					System.out.println("reward is updated from " + prevReward + " to : "+cummReward);
					ADic_Ag.get(action).put(statestr, cummReward);
				}else{
					System.out.println("reward is less than previous one");
				}
			}else{
				System.out.println("new reward is saved as : "+cummReward);
				ADic_Ag.get(action).put(statestr, cummReward);
			}
		}
		/*############## End Learning ############## */
		rewardHist.clear(); // reward history
		prevScore  = 0; // previous score
		stateHist.clear(); // state history
		actionHist.clear(); // action history
	}

	// run the client
	public void run() {
		aRobot.loadLevel(currentLevel);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				
				//newGame일 때 레벨 별 스코어 저장
				if(newGame){
					int score = StateUtil.getScore(ActionRobot.proxy);
					System.out.println("Current Level Score: "+score);
					if(!scores.containsKey(currentLevel)) scores.put(currentLevel, score);
					else if(scores.get(currentLevel) < score) scores.put(currentLevel, score);
				}else{
					//newGame 아니면 학습
					this.learn();
				}
				
				if(trainingFlag){
					//training일 때 성공하면 test로 바꾸고 다시 실행
					trainingFlag = false;
					aRobot.loadLevel(currentLevel);
				}else{
					//test일 때 성공하면 다음 판으로 넘어가기. 
					//21판이거나, newGame == false 일때(즉  test 실패하고 트레이닝 해서 다시 test시 그 판만 성공했을 때)
					//다시 1판부터 test 시작
					if(!newGame || currentLevel==21){
						newGame = true;
						scores.clear();
						currentLevel = 0;
					}
					aRobot.loadLevel(++currentLevel);
				}
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();
				// first shot on this level, try high shot first
				firstShot = true;
				
			} else if (state == GameState.LOST) {
				this.learn();
				if(trainingFlag) {
					//training일 때 실패하면 해당 레벨 다시 시도.
					System.out.println("####### Training Failed - Restart this level #######");
				}else{
					//test일 때 실패하면 training모드로 해당 레벨 다시 시도.
					if(newGame){
						//newGame일 때 총 점수 및 도달 레벨 기록
						System.out.println("####### New Game Test Result #######");
						int totalScore = 0;
						for(Integer key: scores.keySet()){
							totalScore += scores.get(key);
							System.out.println(" Level " + key + " Score: " + scores.get(key) + " ");
						}
						System.out.println("This game's total score: " + totalScore);
						
						//max score 갱신시 Dic 저장
						if(maxScore < totalScore){
							saveMaxResultDic(totalScore,currentLevel,numTimestamp);
							maxScore = totalScore;
						}
						
						Writer scorer;
						try {
							scorer = new BufferedWriter(new FileWriter("gameScore.txt", true));
							scorer.append("\n Total Score: "+totalScore+", Lev "+currentLevel+", at timestamp "+numTimestamp);
							scorer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						newGame = false;
					}
					System.out.println("####### Test Failed - Restart this level #######");
					trainingFlag = true;
				}
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out.println("Unexpected level selection page, go to the last current level : "+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out.println("Unexpected main menu page, go to the last current level : "+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out.println("Unexpected episode menu page, go to the last current level : "+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}
		}
	}

	public GameState solve()
	{
		// Capture the current screenshot and find the slingshots.
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		Rectangle sling = vision.findSlingshotMBR();
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
		
        // Get all the pigs
 		List<ABObject> pigs = vision.findPigsMBR();
		GameState state = aRobot.getState();
		Point targetPt = null;
		
		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			if (!pigs.isEmpty()) {
				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;
				
				int[][] MFCstate = vision.getMBRVision().findMFCState();
				String MFCstateStr = "";
				for(int i = 0 ; i < MFCstate.length;i++){
					for(int j = 0; j < MFCstate[i].length; j++){
						MFCstateStr += MFCstate[i][j];
					}
				}
				int hashMFCstate = MFCstateStr.hashCode();
				
				if(!hashToarrayStateDic.containsKey(hashMFCstate)){
					/*
					BufferedImage stateImg = vision.getMBRVision().getStateImg();
					File outputfile = new File("stateImages/Lev"+currentLevel+"_"+hashMFCstate+".jpg");
					try {
						ImageIO.write(stateImg, "jpg", outputfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					*/
					hashToarrayStateDic.put(hashMFCstate, MFCstate);
				}
				
				useMFEC = true;
				double ag = -1;
				//test 모드일 때 optimal action 찾기
				if(!trainingFlag) {
					ag = getAction(hashMFCstate);
					//test 모드더라도 action이 없으면 training 모드로 변환
					if(ag<0) trainingFlag = true;
				}
				
				
				if(trainingFlag){
					System.out.println("Training phase");
					useMFEC = false;

					if (randomGenerator.nextInt(2) ==0){
						ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));
						targetPt = pig.getCenter();
						System.out.println("Pig is chosen as a target");
					}else{
						List<ABObject> blocks = vision.findBlocksMBR();
						ABObject block = blocks.get(randomGenerator.nextInt(blocks.size()));
						targetPt = block.getCenter();
						System.out.println("Block is chosen as a target");
					}
						
					// estimate the release points
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, targetPt);
					
					// do a high shot when entering a level to find an accurate velocity
					if (firstShot && pts.size() > 1) releasePoint = pts.get(1);
					else if (pts.size() == 1) releasePoint = pts.get(0);
					else if (pts.size() == 2){
						if (randomGenerator.nextInt(2) ==0) releasePoint = pts.get(1);
						else releasePoint = pts.get(0);
					}
					else if(pts.isEmpty()){
							System.out.println("No release point found for the target");
							System.out.println("Try a shot with 45 degree");
								releasePoint = tp.findReleasePoint(sling, Math.PI/4);
					}
					//Do not make a minus angle shot
					if(Math.toDegrees(tp.getReleaseAngle(sling, releasePoint))<0) 
						releasePoint = tp.findReleasePoint(sling, Math.PI/4);
				}else{
					System.out.println("Test phase");
					releasePoint = tp.findReleasePoint(sling, Math.toRadians(ag));
				}
					
				// Get the reference point
				Point refPoint = tp.getReferencePoint(sling);
				
				//Calculate the tapping time according the bird type 
				if (releasePoint != null) {
					double releaseAngle = tp.getReleaseAngle(sling, releasePoint);
					System.out.println("Release Angle: "+ Math.toDegrees(releaseAngle));
					
					int tapInterval = 0;
					switch (aRobot.getBirdTypeOnSling()){
						case RedBird:
							tapInterval = 0; break; 
						case YellowBird:
							tapInterval = 55; break;
						case WhiteBird:
							tapInterval =  65; break;
						case BlackBird:
							tapInterval =  75; break;
						case BlueBird:
							tapInterval =  55; break;
						default:
							tapInterval =  60;
					}
					
					//tap interval을 test/training 동일하게 적용하기 위해 target point 다시 계산
					targetPt = tp.getTrajectoryTarget(sling, releasePoint);
					System.out.println("Estimated end point: ("+targetPt.x+","+targetPt.y+")");
					
					int tapTime = tp.getTapTime(sling, releasePoint, targetPt, tapInterval);
					dx = (int)releasePoint.getX() - refPoint.x;
					dy = (int)releasePoint.getY() - refPoint.y;
					shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
				}else{
					System.err.println("No Release Point Found");
					return state;
				}

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{	
								// Shoot!
								aRobot.cshoot(shot);
								
								// Get a current score
								int currentScore = StateUtil.getCurrentScore(ActionRobot.proxy);
								if(currentScore ==0) currentScore = prevScore;
								System.out.println("Previous Score: "+prevScore+", Current Score: "+currentScore);
								
								if(currentScore >=prevScore){
									/* State */
									stateHist.add(hashMFCstate);
									/* Reward */
									int currentReward = currentScore - prevScore;
									prevScore = prevScore + currentReward;
									rewardHist.add(currentReward);
									System.out.println("Current Reward: "+currentReward);
									/* Action */
									double thisAction = ag;
									if (!useMFEC){
										thisAction = Math.round(Math.toDegrees(tp.getReleaseAngle(sling, releasePoint))*10.0)/10.0;
									}
									actionHist.add(thisAction);
								}
								
								System.out.println("Timestamp : "+numTimestamp);
								if(numTimestamp % 1000 ==0) saveDic(numTimestamp);
								numTimestamp++;
								
								state = aRobot.getState();
								if ( state == GameState.PLAYING ){
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		System.out.println("---------------------");
		return state;
	}
	
	public static void printActionSize(){
		for(double i = 0; i<10;i++){
			System.out.print(i/10.0 +": ");
			for(double j = 0;j<80; j++){
				double action = j+i*0.1;
				action = Math.round(action*10.0)/10.0;
				HashMap<Integer, Integer> SRDic = ADic_Ag.get(action);
				System.out.printf("%1d", SRDic.size());
			}
			System.out.println();
		}
	}
	
	public static void saveDic(int numTimestamp){
		try{
            FileOutputStream fos = new FileOutputStream("AdicMap_"+numTimestamp+".data");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ADic_Ag);
            oos.close();
            fos.close();
            System.out.printf("Serialized Action Dic is saved in AdicMap_ts.data");
         }catch(IOException ioe){
                ioe.printStackTrace();
         }
		
		try{
			  FileOutputStream fos = new FileOutputStream("str2arrayDic_"+numTimestamp+".data");  
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hashToarrayStateDic);
            oos.close();
            fos.close();
            System.out.printf("Serialized str to array state dic is saved in str2arrayDic_ts.data");
         }catch(IOException ioe){
                ioe.printStackTrace();
         }
	}
	
	public static void saveMaxResultDic(int totalScore, int lev, int ts){
		try{
            FileOutputStream fos = new FileOutputStream("AdicMap_"+totalScore+"by Lv"+lev+"at "+ts+".data");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ADic_Ag);
            oos.close();
            fos.close();
            System.out.printf("New max score Adic is saved");
         }catch(IOException ioe){
                ioe.printStackTrace();
         }
		
		try{
			FileOutputStream fos = new FileOutputStream("str2arrayDic_"+totalScore+"by Lv"+lev+"at "+ts+".data");  
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hashToarrayStateDic);
            oos.close();
            fos.close();
            System.out.printf("New max score str2arrayDic is saved");
         }catch(IOException ioe){
                ioe.printStackTrace();
         }
		 
	}
	
	public static double getStateDist(int[][] state1, int[][] state2){
		double sum = 0;
		for(int i = 0 ; i < state1.length;i++){
			for(int j = 0; j < state1[i].length; j++){
				sum += Math.pow((state1[i][j]-state2[i][j]),2);
			}
		}
		if(sum ==0){
			return 0.0;
		}else{
			double dist = Math.sqrt(sum);
			return dist;
		}
	}
	
	public static double getAction(int state){
		int n = 5;
		int maxReward = -1;
		double bestAction = -1;
		int[][] arrayState1 = hashToarrayStateDic.get(state); 
		int[][] arrayState2;
		
		for (Double action : ADic_Ag.keySet()){
			HashMap<Integer, Integer> SRDic = ADic_Ag.get(action);
			int expectedReward = Integer.MIN_VALUE;
			
			if(SRDic.containsKey(state)){
				expectedReward = SRDic.get(state);
			}else{			
				if(SRDic.keySet().size() >=n){
					HashMap<Integer, Double> SDistDic = new HashMap<Integer, Double>();
					for (Integer actionState : SRDic.keySet()){
						arrayState2 = hashToarrayStateDic.get(actionState);
						double dist = getStateDist(arrayState1, arrayState2);
						SDistDic.put(actionState,dist);
					}
					
					List<Integer> topNstates = getTopNStates(SDistDic, n);
					 
					//System.out.println();
					int rewardSum =0;
					for(Integer actionState : topNstates){
						rewardSum += SRDic.get(actionState);
					}
					expectedReward = rewardSum/n;
				}
			}
			 
			if (expectedReward > maxReward){
				maxReward = expectedReward;
				System.out.println("New best angle is " + action.toString() + "(E(reward): "+maxReward+")");
				bestAction = action;
			}
		}
		
		if(maxReward>0)
			return bestAction;
		else{
			System.out.println("No previous action available");
			return -1;
		}
			
	}
	
	public static List<Integer> getTopNStates(final HashMap<Integer, Double> map, int n) {
	    PriorityQueue<Integer> topN = new PriorityQueue<Integer>(n, new Comparator<Integer>() {
	        public int compare(Integer s1, Integer s2) {
	            return Double.compare(map.get(s2), map.get(s1));
	        }
	    });

	    for(Integer key:map.keySet()){
	    	//System.out.println(map.get(key));
	        if (topN.size() < n)
	            topN.add(key);
	        else if (map.get(topN.peek()) > map.get(key)) {
	            topN.poll();
	            topN.add(key);
	        }
	    }
	    
	    List<Integer> topNlist = new ArrayList<Integer>();
	    for (Integer state: topN) {
	    	topNlist.add(state);
	    }
	    
	    return topNlist;
	}

	public static void main(String args[]) {
		MFECAgent na = new MFECAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
