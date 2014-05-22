package es.ehubio.tools;

import java.util.Arrays;

public class Command {
	public interface Interface {
		public String getUsage();

		public int getMinArgs();

		public int getMaxArgs();
		
		public void run( String[] args ) throws Exception;
	}
	
	public static void main( String[] args ) {
		if( args.length == 0 ) {
			System.out.println( "Usage:\n\tCommand <command> [args]" );
			return;
		}		
		try {
			Interface cmd = (Interface)Class.forName("es.ehubio.tools."+args[0]).newInstance();
			int nargs = args.length-1;
			if( nargs < cmd.getMinArgs() || nargs > cmd.getMaxArgs() ) {
				System.out.println( "Usage:\n\tCommand "+args[0]+" "+cmd.getUsage() );
				return;
			}
			cmd.run(Arrays.copyOfRange(args, 1, args.length));
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
}
