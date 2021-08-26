package base.web;

import java.io.IOException;
import java.net.ServerSocket;

public class FreePortReservation {
    private static final int PORT_LOWER_BOUND = 49152;
    private static final int PORT_UPPER_BOUND = 65535;
		private ServerSocket reservation;
		
		public static int getFreePort() throws IOException{
		  return findFreePort();
		}
		
		public static FreePortReservation reserveFreePort() throws IOException {
		  FreePortReservation reservation = new FreePortReservation();
		  reservation.reservation = new ServerSocket(findFreePort());
			return reservation;
		}
		
		public int redeemReservedPort() throws IOException {
			int port = reservation.getLocalPort();
			boolean closed = false;
			int failureCounter = 0;
			while(!closed && failureCounter < 10) {
			  try {
	        reservation.close();
	        closed = true;
	      } catch (IOException e) {
	        ++failureCounter;
	      }
			}
			
			if(!closed) {
			  throw new IOException();
			}
			
			return port;
		}
		
		private static int findFreePort() throws IOException{
			int currentPort = PORT_LOWER_BOUND;
			while(currentPort <= PORT_UPPER_BOUND) {
				try (ServerSocket serverSocket = new ServerSocket(currentPort)) {
					if (serverSocket.isBound() && serverSocket.getLocalPort() == currentPort) {
						return currentPort;
					}
				} catch (IOException e) {
					++currentPort;
				}
			}
    
			throw new IOException("No free port in the <" + PORT_LOWER_BOUND + ", " + PORT_UPPER_BOUND + "> range");
		}
		
		public int peekReservedPort() {
		  return this.reservation != null? this.reservation.getLocalPort() : 0;
		}
	}