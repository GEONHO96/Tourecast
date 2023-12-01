import javax.swing.*;

public class Main extends JFrame {
    protected home h;
    protected static localAttraction la = new localAttraction();
    protected static localWeather lw = new localWeather();
    protected static recommendAttraction ra = new recommendAttraction();
    private static SpotDAO spotDAO = new SpotDAO();
    private static MidFcstInfoService midFcstInfoService = new MidFcstInfoService();

    public static void main(String[] args) throws Exception{
        String currentdate = spotDAO.currentSixOrEighteen();

        midFcstInfoService.setMidta(currentdate);
        midFcstInfoService.getMidLandFcst("11B00000",currentdate);
        midFcstInfoService.getMidLandFcst("11D10000",currentdate);
        midFcstInfoService.getMidLandFcst("11D20000",currentdate);
        midFcstInfoService.getMidLandFcst("11C20000",currentdate);
        midFcstInfoService.getMidLandFcst("11C10000",currentdate);
        midFcstInfoService.getMidLandFcst("11F20000",currentdate);
        midFcstInfoService.getMidLandFcst("11F10000",currentdate);
        midFcstInfoService.getMidLandFcst("11H10000",currentdate);
        midFcstInfoService.getMidLandFcst("11H20000",currentdate);
        midFcstInfoService.getMidLandFcst("11G00000",currentdate);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new home().setVisible(true);
            }
        });
    }
}
