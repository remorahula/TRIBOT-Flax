package scripts;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api2007.*;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import scripts.OsUtils.AntibanUtil;

import java.awt.*;
import java.util.Random;


public class FlaxSpinner extends Script implements Painting
{
    String flaxName = "Flax";

    String spellName = "Spin Flax";

    int reactMin = 150,
        reactMax = 3000,
        reactMean = 670,
        reactSD = 80;
    boolean reactSet = false;


    long startTime = -1;

    int startMageLvl = -1,
            startCraftLevel = -1,
            startMageXp = -1,
            startCraftXp = -1;

    @Override
    public void run()
    {
        startTime = Timing.currentTimeMillis();
        ABCUtil abcUtil = new ABCUtil();

        boolean runScript = true;

        while(runScript)
        {
            if(!reactSet && Login.getLoginState() == Login.STATE.INGAME)
            {
                String pName = Player.getRSPlayer().getName();
                long seed = 0;

                for(char c : pName.toCharArray())
                    seed += c;

                Random r = new Random(seed);
                reactMax += r.nextInt(500);
                reactMean += r.nextInt((100 + 100) + 1) - 100;
                reactSD += r.nextInt(60);
                reactSet = true;
            }


            if (Banking.isInBank())
            {
                if(Banking.isBankScreenOpen())
                {
                    if(Inventory.find(flaxName).length != 25)
                    {
                        if (Inventory.getAll().length > 3)
                        {
                            Banking.depositAllExcept("Nature rune", "Astral rune");
                            Timing.waitCondition(new Condition()
                            {
                                @Override
                                public boolean active()
                                {
                                    return Inventory.getAll().length <= 3;
                                }
                            },General.random(800,3000));
                            General.sleep(General.randomSD(70,1500,485,150));
                        }
                        else
                        {
                            if(Banking.withdraw(25, flaxName)) {
                                Timing.waitCondition(new Condition()
                                {
                                    @Override
                                    public boolean active()
                                    {
                                        return Inventory.find(flaxName).length > 0;
                                    }
                                }, General.random(600, 2000));
                            }
                            General.sleep(General.randomSD(70,1500,465,150));
                        }
                    }
                    else
                    {
                        if (Banking.close()) {
                            Timing.waitCondition(new Condition()
                            {
                                @Override
                                public boolean active()
                                {
                                    return !Banking.isBankScreenOpen();
                                }
                            }, General.random(600, 2000));
                        }
                        General.sleep(General.randomSD(70,1500,435,150));
                    }
                }
                else
                {
                    if (Inventory.find(flaxName).length > 0)
                    {
                        if(Player.getRSPlayer().getAnimation() != -1)
                        {
                            AntibanUtil.PerformTimedActions(abcUtil);
                        }
                        else
                        {
                            General.sleep(General.randomSD(reactMin, reactMax, reactMean, reactSD));
                            if(Magic.selectSpell(spellName))
                            {
                                if (Timing.waitCondition(new Condition()
                                {
                                    @Override
                                    public boolean active()
                                    {
                                        return Player.getRSPlayer().getAnimation() != -1;
                                    }
                                }, General.random(1700, 3800)))
                                {
                                    General.sleep(General.randomSD(70, 1500, 865, 180));
                                }
                                else
                                {
                                    if(Banking.openBank()) {
                                        Timing.waitCondition(new Condition()
                                        {
                                            @Override
                                            public boolean active()
                                            {
                                                return Banking.isBankScreenOpen();
                                            }
                                        }, General.random(1000, 2000));
                                    }

                                    General.sleep(General.randomSD(70, 1500, 435, 150));
                                }
                            }
                        }
                    }
                    else
                    {
                        if(Banking.openBank()) {
                            Timing.waitCondition(new Condition()
                            {
                                @Override
                                public boolean active()
                                {
                                    return Banking.isBankScreenOpen();
                                }
                            }, General.random(1000, 2000));
                        }
                        General.sleep(General.randomSD(70,1500,435,150));
                    }
                }
            }
            else
            {
                General.println("Please start this script in a bank");
                runScript = false;
            }


            General.sleep(10);
        }

        if(Banking.isBankScreenOpen())
            Banking.close();

        General.println("Script ending, logging out");
        Login.logout();
    }

    @Override
    public void onPaint(Graphics g)
    {
        g.setColor(Color.GREEN);

        g.drawString("Flax Spinner",20,20);
        g.drawString("Time Ran: " + Timing.msToString(Timing.currentTimeMillis()-startTime),20,40);
        g.drawString("Magic XP/hr: " + perHour(Skills.getXP(Skills.SKILLS.MAGIC)-startMageXp),20,60);
    }
    private String perHour(int gained) {
        return (((int) ((gained) * 3600000D / (System.currentTimeMillis() - startTime))) + "");
    }

    /*
    static private double nextSkewedBoundedDouble(Random r, double min, double max, double skew, double bias) {
        double range = max - min;
        double mid = min + range / 2.0;
        double unitGaussian = r.nextGaussian();
        double biasFactor = Math.exp(bias);
        double retval = mid+(range*(biasFactor/(biasFactor+Math.exp(-unitGaussian/skew))-0.5));
        return retval;
    }*/
}